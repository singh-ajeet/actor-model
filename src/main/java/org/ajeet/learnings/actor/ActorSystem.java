package org.ajeet.learnings.actor;

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

    private final Map<String, Actor> actorRegistry = new ConcurrentHashMap<>();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);


    public <M> Actor<M> create(Behavior<M> behavior) {
        return _createActor(behavior, 0);
    }

    public <M> Actor<M> create(Behavior<M> behavior, int actorMessageQueueSize) {
        if(actorMessageQueueSize <= 0)
            throw new IllegalArgumentException("Specified actor message queue must be greater then 0.");

        return _createActor(behavior, actorMessageQueueSize);
    }

    private <M> Actor<M> _createActor(Behavior<M> behavior, int actorMessageQueueSize) {
        String actorId = generateActorId();
        Actor<M> actor =  new Actor<M>(behavior, actorId, actorMessageQueueSize);
        actorRegistry.put(actorId, actor);
        return actor;
    }

    //TODO - Need to find a better way to assign a unique id to each actor
    private String generateActorId() {
        return "actor-" + UUID.randomUUID();
    }

    public <M> Actor<M> createAndStart(Behavior<M> behavior) {
        Actor<M> a = create(behavior);
        new Thread(a).start();
        return a;
    }

    /**
     * Stop Actor System, it will gracefully stop all actors
     */
    public void stop(){
        if(isStopped.get()){
            log.log(Level.WARNING, "Shutdown is already in progress.");
        }

        log.log(Level.INFO, "Shutting down actor system.");
        isStopped.compareAndSet(false, true);
        shutdownActors();
    }

    private void shutdownActors() {
        //TODO
    }
}
