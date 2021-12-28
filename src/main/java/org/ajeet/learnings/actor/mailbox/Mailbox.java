package org.ajeet.learnings.actor.mailbox;

/**
 * MailBox must use a data structure that is thread safe
 *
 * @param <T>
 */
public interface Mailbox<T> {
    public T remove();
    public boolean add(T message);
    public void clear();
    public int getSize();
}
