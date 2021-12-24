package org.ajeet.learnings.actor;

public interface Behavior<Msg> {
    /**
     * @param self
     * @param msg
     * @return - `false` - stop the actor; `true` - continue
     */
    public abstract boolean onReceive(Actor<Msg> self, Msg msg);

    /**
     * DeadException thrown by the actor `self`. The thread is dead.
     *
     * @param self
     * @param e
     */
    public abstract void onException(Actor<Msg> self, Exception e);
}
