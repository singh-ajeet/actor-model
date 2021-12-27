package org.ajeet.learnings.actor;

import org.ajeet.learnings.actor.commons.DeadException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Actor<T, R> {
    private static final Logger LOG = Logger.getLogger(Actor.class.getName());
    
    private final FIFOMailbox<Tuple<T, R>> queue;
    private final Action<T, R> action;
    public final String actorId;
    private final ExecutorService executorService;
    private final int batchSize;

    private volatile boolean isStopped = false;
    private State state;

    Actor(Action<T, R> action,
          String actorId,
          int actorMessageQueueSize,
          ExecutorService executorService,
          int batchSize) {

        this.state = State.ALIVE;
        this.action = action;
        this.actorId = actorId;
        this.executorService = executorService;
        this.batchSize = batchSize;

        if(actorMessageQueueSize == 0)
            this.queue = new FIFOMailbox<Tuple<T, R>>();
        else
            this.queue = new FIFOMailbox<Tuple<T, R>>(actorMessageQueueSize);
    }

    public CompletableFuture<R> send(Message<T> msg) throws DeadException {
        if (isStopped) {
            throw new DeadException("Shutting down actor system");
        }

        CompletableFuture<R> future = new CompletableFuture<>();
        Runnable runnable = () -> {
            int size = queue.getSize();
            queue.add(new Tuple<>(msg, result -> future.complete(result)));
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

    private static class Tuple<T, R> {
        private final Message<T> message;
        private final Consumer<R> consumer;

        private Tuple(Message<T> message, Consumer<R> consumer) {
            this.message = message;
            this.consumer = consumer;
        }

        private void consume(R result){
            consumer.accept(result);
        }
    }
}