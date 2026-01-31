package com.bulefire.neuracraft.core.agent.entity;

import com.bulefire.neuracraft.compatibility.entity.APlayer;

public record AgentResponse(String msg, State state, APlayer player) {
    
    public enum State {
        NORMAL,
        START_MCP_CALL,
        MCP_CALLING,
        FINISH_MCP_CALL
    }
}
