package com.bulefire.neuracraft.testutil;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.core.agent.Agent;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.agent.entity.AgentResponse;
import com.bulefire.neuracraft.core.util.AgentOutOfTime;
import com.bulefire.neuracraft.core.util.UnSupportFormattedMessage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FakeAgent implements Agent {
    private final UUID uuid;
    private final String name;
    private final Set<APlayer> players = new HashSet<>();
    private final Set<APlayer> admins = new HashSet<>();

    public FakeAgent() {
        this(UUID.randomUUID(), "fake");
    }

    public FakeAgent(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public @NotNull AgentResponse sendMessage(@NotNull AgentMessage message) throws AgentOutOfTime, UnSupportFormattedMessage {
        return new AgentResponse("ok", AgentResponse.State.NORMAL, message.player());
    }

    @Override public void setName(@NotNull String name) {}
    @Override public @NotNull String getName() { return name; }
    @Override public @NotNull UUID getUUID() { return uuid; }
    @Override public void addPlayer(@NotNull APlayer player) { players.add(player); }
    @Override public void removePlayer(@NotNull APlayer player) { players.remove(player); }
    @Override public @NotNull Set<APlayer> getPlayers() { return players; }
    @Override public void addAdmin(@NotNull APlayer player) { admins.add(player); }
    @Override public boolean hasAdmin(@NotNull APlayer player) { return admins.contains(player); }
    @Override public void removeAdmin(@NotNull APlayer player) { admins.remove(player); }
    @Override public @NotNull Set<APlayer> getAdmins() { return admins; }
    @Override public @NotNull String getModelName() { return "fake"; }
    @Override public @NotNull String getDisPlayName() { return "Fake"; }
    @Override public boolean isMCPCalling() { return false; }
    @Override public boolean isMessaging() { return false; }
    @Override public void lockMessageLock() {}
    @Override public void releaseMessageLock() {}
    @Override public void releaseAllMessageLock() {}
    @Override public void saveToFile(@NotNull Path path) {}
    @Override public void loadFromFile(@NotNull Path path) {}
    @Override public String getSuffix() { return "fake"; }
    @Override public void reloadConfig() {}
}
