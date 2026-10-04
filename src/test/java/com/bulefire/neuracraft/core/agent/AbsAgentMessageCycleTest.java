package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.agent.entity.AgentResponse;
import com.bulefire.neuracraft.core.util.AgentOutOfTime;
import com.bulefire.neuracraft.core.util.UnSupportFormattedMessage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the REAL {@link AbsAgent} message-cycle protocol (Plan A).
 *
 * <p>Unlike a protocol model, this drives the concrete implementation's
 * {@code beginMessageCycle} / {@code endMessageCycle} / {@code abortMessage} /
 * {@code sendMessage}, so it fails if the real class regresses.</p>
 */
class AbsAgentMessageCycleTest {

    private static final APlayer PLAYER = new APlayer("tester", UUID.randomUUID());

    /** Minimal concrete agent: canned model reply, no file or Minecraft access. */
    private static final class TestAgent extends AbsAgent {
        private volatile boolean replyWithToolCall;

        TestAgent() {
            super("t", UUID.randomUUID(), new HashSet<>(), new HashSet<>(),
                    "test", "Test", "test", 1000);
        }

        void replyWithToolCall(boolean value) { this.replyWithToolCall = value; }

        @Override
        protected @NotNull String message(@NotNull AgentMessage message) {
            if (replyWithToolCall) {
                setToolCalls(List.of());
                // No OpenAiToolCall instances available without Minecraft types, so
                // signal the tool path through the JSON fallback instead.
                return "{\"tool_call\":{\"id\":\"c1\",\"parameters\":{}}}";
            }
            return "hello";
        }

        @Override public void saveToFile(@NotNull Path path) {}
        @Override public void loadFromFile(@NotNull Path path) {}
        @Override public void reloadConfig() {}
    }

    private static AgentMessage userMessage() {
        return new AgentMessage(List.of(new com.bulefire.neuracraft.compatibility.entity.Content("text", "hi")), PLAYER);
    }

    @Test
    void singleCycleAdmitsOneOwnerAtATime() {
        TestAgent agent = new TestAgent();
        assertTrue(agent.beginMessageCycle(), "first acquire must succeed");
        assertFalse(agent.beginMessageCycle(), "second acquire must be rejected, not queued");
        assertTrue(agent.isMessaging(), "state must read as busy while the cycle is held");

        agent.endMessageCycle();
        assertFalse(agent.isMessaging(), "endMessageCycle must return the agent to idle");
        assertTrue(agent.beginMessageCycle(), "cycle must be reusable");
    }

    @Test
    void foreignThreadCannotEndOrAbortTheOwnersCycle() throws Exception {
        TestAgent agent = new TestAgent();
        assertTrue(agent.beginMessageCycle());

        runOnOtherThread(agent::endMessageCycle);
        assertFalse(agent.beginMessageCycle(), "a foreign endMessageCycle must not release the cycle");

        runOnOtherThread(agent::abortMessage);
        assertFalse(agent.beginMessageCycle(), "a foreign abortMessage must not release the cycle");

        agent.endMessageCycle();
        assertTrue(agent.beginMessageCycle(), "the owner can still release its own cycle");
    }

    @Test
    void sendMessageKeepsStateNonIdleAcrossAnMcpRound() throws Exception {
        TestAgent agent = new TestAgent();
        assertTrue(agent.beginMessageCycle());

        agent.replyWithToolCall(true);
        AgentResponse response = agent.sendMessage(userMessage());

        assertTrue(response.state() == AgentResponse.State.START_MCP_CALL
                        || response.state() == AgentResponse.State.MCP_CALLING,
                "a tool-call reply must be reported as an MCP round, got " + response.state());
        assertTrue(agent.isMCPCalling(), "state must be MCP_CALLING during the MCP loop");
        assertFalse(agent.beginMessageCycle(),
                "no other thread may start a cycle while the MCP loop runs");

        // Final answer for the same cycle.
        agent.replyWithToolCall(false);
        AgentResponse finalResponse = agent.sendMessage(userMessage());
        assertEquals(AgentResponse.State.FINISH_MCP_CALL, finalResponse.state(),
                "returning to a normal answer must be reported as finishing the MCP call");
        assertFalse(agent.beginMessageCycle(),
                "the cycle must remain held until endMessageCycle, closing the mid-cycle race window");
    }

    @Test
    void sendMessageIsUsableWithoutAnExplicitCycleAndDoesNotStrandTheAgent() throws Exception {
        TestAgent agent = new TestAgent();
        // A direct caller (e.g. a plugin) that never called beginMessageCycle.
        AgentResponse response = agent.sendMessage(userMessage());
        assertEquals(AgentResponse.State.NORMAL, response.state());
        assertFalse(agent.isMessaging(),
                "sendMessage must not strand the agent in a busy state when it owns the cycle itself");
    }

    @Test
    void sendMessageRejectsAConcurrentDirectCallerInsteadOfQueueing() throws Exception {
        TestAgent agent = new TestAgent();
        assertTrue(agent.beginMessageCycle(), "simulate an in-flight cycle on this thread");

        // A *different* thread calling sendMessage directly must be rejected.
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread foreign = new Thread(() -> {
            try {
                agent.sendMessage(userMessage());
            } catch (Throwable t) {
                thrown.set(t);
            }
        }, "foreign-sender");
        foreign.start();
        foreign.join(5000);
        assertFalse(foreign.isAlive(), "foreign sender must not block");

        assertTrue(thrown.get() instanceof AgentOutOfTime,
                "a concurrent direct sendMessage must be rejected with AgentOutOfTime, got " + thrown.get());
    }

    @Test
    void ownerThreadMayReenterSendMessageWithinItsOwnCycle() throws Exception {
        // The controller's own path: beginMessageCycle(), then sendMessage() for the
        // user turn and again for each tool-result round - all on the same thread.
        TestAgent agent = new TestAgent();
        assertTrue(agent.beginMessageCycle());
        assertEquals(AgentResponse.State.NORMAL, agent.sendMessage(userMessage()).state());
        assertEquals(AgentResponse.State.NORMAL, agent.sendMessage(userMessage()).state());
        agent.endMessageCycle();
        assertFalse(agent.isMessaging());
    }

    @Test
    void concurrentBurstAdmitsExactlyOneOwner() throws Exception {
        TestAgent agent = new TestAgent();
        int threads = 32;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger winners = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    if (agent.beginMessageCycle()) winners.incrementAndGet();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }, "burst-" + i).start();
        }
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertEquals(1, winners.get(), "exactly one thread may own the cycle at a time");
    }

    @Test
    void workerWaitingOnTheServerThreadDoesNotHoldTheInstantLock() throws Exception {
        // Plan A's core property: while the worker waits for the server thread
        // (onServerThread -> future.get), it must not hold sendLock, so the server
        // thread can always take it. This is the old P0-3 lock inversion.
        TestAgent agent = new TestAgent();
        assertTrue(agent.beginMessageCycle());

        CountDownLatch workerWaiting = new CountDownLatch(1);
        CountDownLatch serverDone = new CountDownLatch(1);
        CountDownLatch serverAcquired = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            try {
                workerWaiting.countDown();
                serverDone.await(5, TimeUnit.SECONDS);   // stands in for future.get()
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                agent.endMessageCycle();
            }
        }, "agent-worker");

        Thread serverThread = new Thread(() -> {
            agent.lockMessageLock();                     // the server thread needs the lock
            try {
                serverAcquired.countDown();
            } finally {
                agent.releaseMessageLock();
            }
            serverDone.countDown();
        }, "server-thread");

        worker.start();
        assertTrue(workerWaiting.await(5, TimeUnit.SECONDS));
        serverThread.start();

        assertTrue(serverAcquired.await(5, TimeUnit.SECONDS),
                "the server thread must acquire the agent lock while the worker waits for it");
        serverThread.join(5000);
        assertFalse(serverThread.isAlive(), "server thread must not deadlock");
        worker.join(5000);
        assertFalse(worker.isAlive(), "worker must finish");
    }

    private static void runOnOtherThread(Runnable action) throws Exception {
        Thread thread = new Thread(action, "foreign");
        thread.start();
        thread.join(5000);
        assertFalse(thread.isAlive(), "foreign thread must finish");
    }

    @SuppressWarnings("unused")
    private static Set<APlayer> emptyPlayers() { return new HashSet<>(); }
}
