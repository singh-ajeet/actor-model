package org.ajeet.learnings.actor;

import org.ajeet.learnings.actor.commons.DeadException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ActorTester {
    private static ActorSystem actorSystem;

    @BeforeAll
    public static void setup() {
        actorSystem = new ActorSystem(10);
      //  actorSystem = new ActorSystem(Executors.newFixedThreadPool(10));
    }

    @AfterAll
    public static void cleanup(){
        actorSystem.shutdown();
    }

    @Test
    public void testActorWithReturn() throws DeadException, InterruptedException, DeadException, ExecutionException {
        Action<String, Integer> behavior = new Action<String, Integer>() {
            @Override
            public Integer onMessage(Message<String> msg) {
                System.out.println("Got input: " + msg);
                return msg.input.length();
            }

            @Override
            public Integer onException(Throwable ex) {
                ex.printStackTrace();
                return -1;
            }
        };


        Actor<String, Integer> actor = actorSystem.actorBuilderWithAction(behavior)
                .withActorId("MyActor")
                .withMessageBatchSize(5)
                .build();

        CompletableFuture<Integer> future1 = actor.send(new Message<>(actor.actorId, "Something"));
        CompletableFuture<Integer> future2 = actor.send(new Message<>(actor.actorId, "Someone"));

        assertEquals(9, future1.get());
        assertEquals(7, future2.get());
    }

    @Test
    public void testActorWithVoid() throws DeadException, InterruptedException, DeadException, ExecutionException {
        Action<String, Void> behavior = new Action<String, Void>() {
            @Override
            public Void onMessage(Message<String> msg) {
                System.out.println("Got input: " + msg);
                return null;
            }

            @Override
            public Void onException(Throwable ex) {
                ex.printStackTrace();
                return null;
            }
        };

        Actor<String, Void> actor = actorSystem.actorBuilderWithAction(behavior)
                .withActorId("MyAnotherActor")
                .withMessageBatchSize(5)
                .build();

        CompletableFuture<Void> future1 = actor.send(new Message<>(actor.actorId, "Something"));
        CompletableFuture<Void> future2 = actor.send(new Message<>(actor.actorId, "Someone"));

        assertNull(future1.get());
        assertNull(future2.get());
    }
}