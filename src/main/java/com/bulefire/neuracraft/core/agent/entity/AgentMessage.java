package com.bulefire.neuracraft.core.agent.entity;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.entity.Content;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AgentMessage(List<Content> messages, APlayer player) {
    public void toFormattedMessages() {
        messages.set(0, new Content("text", player.toFormatedString()+messages.get(0)));
    }
    
    @Contract(pure = true)
    public @NotNull List<Content> getFormattedMessages() {
        List<Content> result = new ArrayList<>(messages);
        result.set(0, new Content("text", player.toFormatedString()+result.get(0)));
        return result;
    }
}
