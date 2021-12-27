package org.ajeet.learnings.actor.commons;

public class DeadException extends RuntimeException {

    public DeadException(String msg) {
        super(msg);
    }
}