package org.ajeet.learnings.actor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ActorTester {
    private static ActorSystem actorSystem;

    @BeforeAll
    public static void setup(){
        actorSystem = new ActorSystem();
    }

    @Test
    public void testActor() throws DeadException, InterruptedException {
        Behavior<String> behavior = new Behavior<String>() {
            @Override
            public boolean onReceive(Actor<String> self, String msg) {
                System.out.println("Got: " + msg);
                return !msg.equals("stop");
            }

            @Override
            public void onException(Actor self, Exception e) {}
        };

        Actor<String> actor = actorSystem.createAndStart(behavior);

        actor.send("hello");
        actor.send("world");

        Thread.sleep(1000);

        actor.send("stop");
    }
}
