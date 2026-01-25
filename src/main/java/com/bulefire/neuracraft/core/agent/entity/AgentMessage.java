package com.bulefire.neuracraft.core.agent.entity;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record AgentMessage(String msg, APlayer player) {
    @Contract(pure = true)
    public @NotNull String toFormatedMessage() {
        return player().toFormatedString() + ' ' + msg;
    }
}
