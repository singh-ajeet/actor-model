package org.ajeet.learnings.actor;

import org.ajeet.learnings.actor.commons.Tuple;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ActorSystem {
    private final Logger log = Logger.getLogger(ActorSystem.class.getName());
    private final ExecutorService executorService;
    //private final int BATCH_SIZE = 10;
    private final Map<String, Actor> actorRegistry = new ConcurrentHashMap<>();
    private volatile boolean isStopped = false;

    public ActorSystem(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ActorSystem(int threadPoolSize) {
        if(! (threadPoolSize > 0))
            throw new IllegalArgumentException("Thread pool size must be greater then 0");

        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

/*
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
*/

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

    public <T, R> ActorBuilder<T, R> actorBuilderWithAction(Action<T, R> action) {
        return new ActorBuilder<>(this, action);
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

    static class ActorBuilder<T, R> {

        private final ActorSystem actorSystem;
        private final Action<T, R> action;
        private int messageBatchSize;

        private Mailbox<Tuple<T, R>> mailbox;
        private String actorId;

        private ActorBuilder(ActorSystem actorSystem, Action<T, R> action) {
            if(action == null )
                throw new NullPointerException("Action cant be null.");

            this.actorSystem = actorSystem;
            this.action = action;
        }

        public ActorBuilder<T, R> withActorId(String actorId){
            this.actorId  = actorId;
            return this;
        }

        public ActorBuilder<T, R> withMailBox(Mailbox<Tuple<T, R>> mailbox){
            this.mailbox  = mailbox;
            return this;
        }

        public ActorBuilder<T, R> withMessageBatchSize(int messageBatchSize){
            this.messageBatchSize  = messageBatchSize;
            return this;
        }

        public Actor<T, R> build(){
            if(mailbox == null)
                mailbox = new FIFOMailbox<Tuple<T, R>>();

            if(actorId == null )
                actorId = generateActorId();

            Actor<T, R> actor =  new Actor<T, R>(action, actorId,mailbox, actorSystem.executorService, messageBatchSize);
            actorSystem.actorRegistry.put(actorId, actor);
            return actor;
        }

        //TODO - Need to find a better way to assign a unique id to each actor
        private String generateActorId() {
            return "actor-" + UUID.randomUUID();
        }
    }
}
