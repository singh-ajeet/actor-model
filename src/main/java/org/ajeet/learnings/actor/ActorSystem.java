package org.ajeet.learnings.actor;

import org.ajeet.learnings.actor.mailbox.FIFOMailbox;
import org.ajeet.learnings.actor.mailbox.Mailbox;

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

    /**
     * Stop Actor System, it will gracefully stop all actors
     */
    public void shutdown(){
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

    static final class ActorBuilder<T, R> {

        private final ActorSystem actorSystem;
        private final Action<T, R> action;
        private int messageBatchSize;

        private Mailbox<Actor.Tuple<T, R>> mailbox;
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

        public ActorBuilder<T, R> withMailBox(Mailbox<Actor.Tuple<T, R>> mailbox){
            this.mailbox  = mailbox;
            return this;
        }

        public ActorBuilder<T, R> withMessageBatchSize(int messageBatchSize){
            this.messageBatchSize  = messageBatchSize;
            return this;
        }

        public Actor<T, R> build(){
            if(mailbox == null)
                mailbox = new FIFOMailbox<Actor.Tuple<T, R>>();

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
