package org.ajeet.learnings.actor;

public interface Mailbox<Msg> {
    public Msg remove();
    public boolean add(Msg message);
    public void clear();
    public int getSize();
}
