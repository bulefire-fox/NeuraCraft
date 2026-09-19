package com.bulefire.neuracraft.core.agent.entity;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.entity.Content;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AgentMessage(
        List<Content> messages,
        APlayer player,
        Kind kind,
        List<ToolResult> toolResults
) {
    public AgentMessage(List<Content> messages, APlayer player) {
        this(messages, player, Kind.USER, List.of());
    }

    public AgentMessage {
        messages = messages == null ? new ArrayList<>() : messages;
        kind = kind == null ? Kind.USER : kind;
        toolResults = toolResults == null ? List.of() : List.copyOf(toolResults);
    }

    public static AgentMessage toolResults(List<ToolResult> results, APlayer player) {
        return new AgentMessage(List.of(), player, Kind.TOOL_RESULTS, results);
    }

    public boolean isToolResults() {
        return kind == Kind.TOOL_RESULTS;
    }

    public void toFormattedMessages() {
        messages.set(0, new Content("text", player.toFormatedString()+messages.getFirst()));
    }
    
    @Contract(pure = true)
    public @NotNull List<Content> getFormattedMessages() {
        List<Content> result = new ArrayList<>(messages);
        result.set(0, new Content("text", player.toFormatedString()+result.getFirst()));
        return result;
    }

    public @NotNull String getText() {
        return messages.stream()
                .filter(content -> Content.Type.TEXT.equals(content.type()))
                .map(Content::textOrThrow)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    public enum Kind {
        USER,
        TOOL_RESULTS
    }

    public record ToolResult(String toolCallId, List<Content> content) {
        public ToolResult {
            content = content == null ? List.of() : List.copyOf(content);
        }
    }
}
