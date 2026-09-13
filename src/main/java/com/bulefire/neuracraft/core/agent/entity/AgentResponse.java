package com.bulefire.neuracraft.core.agent.entity;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.core.inside.model.openai.OpenAiToolCall;

import java.util.List;

public record AgentResponse(String msg, State state, APlayer player, List<OpenAiToolCall> toolCalls) {
    public AgentResponse(String msg, State state, APlayer player) {
        this(msg, state, player, List.of());
    }

    public AgentResponse {
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public boolean hasToolCalls() {
        return !toolCalls.isEmpty();
    }
    
    public enum State {
        NORMAL,
        START_MCP_CALL,
        MCP_CALLING,
        FINISH_MCP_CALL
    }
}
