package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerManagerTest {
    @Test
    void missingPlayerHasNoAgentInsteadOfSentinelUuid() {
        PlayerManager manager = new PlayerManager();
        APlayer player = new APlayer("player", UUID.randomUUID());

        assertNull(manager.getPlayerAgentUUID(player));
        manager.addPlayer(player, UUID.randomUUID());
        manager.removePlayer(player);
        assertNull(manager.getPlayerAgentUUID(player));
    }
}
