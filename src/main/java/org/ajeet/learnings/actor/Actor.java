package org.ajeet.learnings.actor;

import org.ajeet.learnings.actor.commons.DeadException;
import org.ajeet.learnings.actor.commons.Tuple;

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
    private State state;

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
        if (isStopped) {
            throw new DeadException("Shutting down actor system");
        }

        CompletableFuture<R> future = new CompletableFuture<>();
        Runnable runnable = () -> {
            int size = queue.getSize();

            Consumer<R> consumer = result -> future.complete(result);
            Tuple<T, R> tuple = new Tuple(msg, consumer);
            queue.add(tuple);
            if (size == 0)
                processMessage();
        };
        executorService.execute(runnable);
        return future;
    }

    private void processMessage() {
        int processed = 0;
        while(queue.getSize() > 0){
            try {
                Tuple<T, R> tuple = queue.remove();
                R result = action.onMessage(tuple.message);
                tuple.consume(result);
            } catch (Exception ex){
                action.onException(ex);
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

    public void close(){
        LOG.log(Level.INFO, "Shutting down " + this);
        isStopped = true;
    }
}