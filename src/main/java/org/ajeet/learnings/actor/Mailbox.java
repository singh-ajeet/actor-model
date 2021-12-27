package org.ajeet.learnings.actor;

public interface Mailbox<T> {
    public T remove();
    public boolean add(T message);
    public void clear();
    public int getSize();
}
