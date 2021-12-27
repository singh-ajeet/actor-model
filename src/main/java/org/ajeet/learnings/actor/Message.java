package org.ajeet.learnings.actor;

public final class Message<T> {
    public final String actorId;
    public final T input;

    public Message(String actorId, T input) {
        this.actorId = actorId;
        this.input = input;
    }

    @Override
    public String toString() {
        return "Message{" +
                "actorId='" + actorId + '\'' +
                ", input=" + input +
                '}';
    }
}
