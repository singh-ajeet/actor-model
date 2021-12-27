package org.ajeet.learnings.actor;

import org.ajeet.learnings.actor.commons.DeadException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ActorSystem {
    private final Logger log = Logger.getLogger(ActorSystem.class.getName());
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final int BATCH_SIZE = 10;
    private final Map<String, Actor> actorRegistry = new ConcurrentHashMap<>();
    private volatile boolean isStopped = false;

    public <T, R> Actor<T, R> create(Action<T, R> action) {
        return _createActor(action, 0);
    }

    public <T, R> Actor<T, R> create(Action<T, R> action, int actorMessageQueueSize) {
        if(isStopped)
            throw new DeadException("Shuting down actor system");

        if(actorMessageQueueSize <= 0)
            throw new IllegalArgumentException("Specified actor message queue must be greater then 0.");

        return _createActor(action, actorMessageQueueSize);
    }

    private <T, R> Actor<T, R> _createActor(Action<T, R> behavior, int actorMessageQueueSize) {
        String actorId = generateActorId();
        Actor<T, R> actor =  new Actor<T, R>(behavior, actorId, actorMessageQueueSize, executorService, BATCH_SIZE);
        actorRegistry.put(actorId, actor);
        return actor;
    }

    //TODO - Need to find a better way to assign a unique id to each actor
    private String generateActorId() {
        return "actor-" + UUID.randomUUID();
    }

    /**
     * Stop Actor System, it will gracefully stop all actors
     */
    public void stop(){
        if(isStopped){
            log.log(Level.WARNING, "Shutdown is already in progress.");
        }

        log.log(Level.INFO, "Shutting down actor system.");
        isStopped =  true;
        shutdownActors();
    }

    private void shutdownActors() {
        for(Actor actor : actorRegistry.values()){
            try {
                actor.close();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
