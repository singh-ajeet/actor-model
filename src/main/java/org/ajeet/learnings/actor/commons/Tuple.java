package org.ajeet.learnings.actor.commons;

import org.ajeet.learnings.actor.Message;

import java.util.function.Consumer;

public final class Tuple<T, R> {
    public final Message<T> message;
    public final Consumer<R> consumer;

    public Tuple(Message<T> message, Consumer<R> consumer) {
        this.message = message;
        this.consumer = consumer;
    }

    public void consume(R result){
        consumer.accept(result);
    }
}
