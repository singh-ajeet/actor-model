package org.ajeet.learnings.actor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is a First In First Ourt based implementation of MailBox
 */
public class FIFOMailbox<Msg> implements Mailbox<Msg> {
    private final BlockingQueue<Msg> queue;

    public FIFOMailbox(int capacity) {
        if(! (capacity > 0))
            throw new IllegalArgumentException("Capacity must be greater then 0.");

        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public FIFOMailbox() {
        this.queue = new LinkedBlockingQueue<>();
    }

    @Override
    public Msg remove() {
        return queue.poll();
    }

    @Override
    public boolean add(Msg message) {
        return queue.offer(message);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public int getSize() {
        return queue.size();
    }
}