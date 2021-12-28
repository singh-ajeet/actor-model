package org.ajeet.learnings.actor;

import org.ajeet.learnings.actor.commons.DeadException;
import org.ajeet.learnings.actor.mailbox.Mailbox;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Actor<T, R> {
    private static final Logger LOG = Logger.getLogger(Actor.class.getName());
    
    private final Mailbox<Tuple<T, R>> queue;
    private final Action<T, R> action;
    public final String actorId;
    private final ExecutorService executorService;
    private final int batchSize;

    private volatile boolean isStopped = false;
    private volatile State state;

    Actor(Action<T, R> action,
          String actorId,
          Mailbox<Tuple<T, R>> queue,
          ExecutorService executorService,
          int batchSize) {

        this.state = State.ALIVE;
        this.action = action;
        this.actorId = actorId;
        this.executorService = executorService;
        this.batchSize = batchSize;
        this.queue = queue;
    }

    public CompletableFuture<R> send(Message<T> msg) throws DeadException {
        if (isDead()) {
            throw new DeadException("Shutting down actor.");
        }

        CompletableFuture<R> future = new CompletableFuture<>();

        Runnable runnable = () -> {
            Tuple<T, R> tuple = Tuple.of(msg, future);

            int size = queue.getSize();
            queue.add(tuple);
            
            if (size == 0)
                processMessage();
        };
        executorService.execute(runnable);
        return future;
    }

    public boolean isDead() {
        return isStopped || State.DEAD.equals(state);
    }

    private void processMessage() {
        int processed = 0;
        while(queue.getSize() > 0){
            if (isDead()) {
                throw new DeadException("Shutting down actor.");
            }

            Tuple<T, R> tuple = queue.remove();
            try {
                R result = action.onMessage(tuple.message);
                tuple.resultFuture.complete(result);
            } catch (Exception ex){
                action.onException(ex);
                tuple.resultFuture.completeExceptionally(ex);
            }
            processed++;
            // If processed message are exceeding required quota then break it
            if(processed >= batchSize){
                break;
            }
        }
        // If messages are remaining in queue then put this actor back for execution
        if(queue.getSize() > 0)
            executorService.execute(() -> processMessage());
    }

    @Override
    public String toString() {
        return "Actor {" + "actorId='" + actorId + '}';
    }

    public synchronized void close(){
        if(isStopped || State.DEAD.equals(state))
            throw new DeadException("Actor is already dead");

        LOG.log(Level.INFO, "Shutting down " + this);
        isStopped = true;
        state = State.DEAD;
    }

    static final class Tuple<T, R> {
        final Message<T> message;
        final CompletableFuture<R> resultFuture;

        private Tuple(Message<T> message, CompletableFuture<R> resultFuture) {
            this.message = message;
            this.resultFuture = resultFuture;
        }

        static <T, R> Tuple of(Message<T> message, CompletableFuture<R> resultFuture) {
            return new Tuple(message, resultFuture);
        }
    }
}