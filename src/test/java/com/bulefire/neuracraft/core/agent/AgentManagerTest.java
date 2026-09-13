package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.testutil.FakeAgent;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AgentManagerTest {
    @Test
    void createsFindsAndRemovesAgent() {
        AgentManager manager = new AgentManager();
        UUID uuid = UUID.randomUUID();
        manager.registerAgentMapping("fake", () -> new FakeAgent(uuid, "room"));

        Agent created = manager.createAgent("fake");

        assertSame(created, manager.getAgent(uuid));
        assertEquals(1, manager.getAgentByName("room").size());
        assertSame(created, manager.removeAgent(uuid));
        assertNull(manager.getAgent(uuid));
    }

    @Test
    void duplicateUuidUsesLatestAgent() {
        AgentManager manager = new AgentManager();
        UUID uuid = UUID.randomUUID();
        FakeAgent first = new FakeAgent(uuid, "first");
        FakeAgent second = new FakeAgent(uuid, "second");

        manager.addAgent(first);
        manager.addAgent(second);

        assertSame(second, manager.getAgent(uuid));
    }

    @Test
    void identifiesRegisteredConfigSuffix() {
        AgentManager manager = new AgentManager();
        manager.registerAgentPathConsumer(path -> path.toString().endsWith(".fake") ? "fake" : null);

        assertEquals("fake", manager.getAgentByConfigFilePath(Path.of("room.fake")));
        assertNull(manager.getAgentByConfigFilePath(Path.of("room.json")));
    }
}
