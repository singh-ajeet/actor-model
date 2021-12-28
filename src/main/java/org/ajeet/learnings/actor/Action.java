package org.ajeet.learnings.actor;

/**
 * This class defines the contract to create an Actor. Actor's behavior or action will be defined by the
 * implementation of this interface
 *
 * @param <T>  input Message type
 * @param <R>  return type
 *
 */
public interface Action<T, R>  {
    public R onMessage(Message<T>input);
    public R onException(Throwable ex);
}
