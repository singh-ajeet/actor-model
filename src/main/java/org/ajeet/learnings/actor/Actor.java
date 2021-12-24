package org.ajeet.learnings.actor;

public class Actor<Msg> implements Runnable {
    private final FIFOMailbox<Msg> queue;
    private final Behavior<Msg> behavior;
    private final String actorId;
    private State state;

    Actor(Behavior<Msg> behavior, String actorId, int actorMessageQueueSize) {
        this.state = State.ALIVE;
        this.behavior = behavior;
        this.actorId = actorId;

        if(actorMessageQueueSize == 0)
            this.queue = new FIFOMailbox<Msg>();
        else
            this.queue = new FIFOMailbox<Msg>(actorMessageQueueSize);
    }

    Actor(Behavior<Msg> behavior, String actorId) {
        this(behavior, actorId, 0);
    }


    public void run() {
        try {
            while (behavior.onReceive(this, queue.remove())) {}
        } catch (Exception ex) {
            behavior.onException(this, ex);
        }
        this.state = State.DEAD;
        this.queue.clear();
    }

    /**
     * Try to send "msg" to the actor
     *
     * @param msg
     * @return true if successfully sent, false - if not
     * @throws DeadException - if the actor is already dead
     */
    public boolean send(Msg msg) throws DeadException {
        if (state == State.DEAD) {
            throw new DeadException();
        }
        return queue.add(msg);
    }
}