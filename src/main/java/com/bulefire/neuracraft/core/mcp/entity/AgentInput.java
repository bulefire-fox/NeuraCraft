package com.bulefire.neuracraft.core.mcp.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class AgentInput {
    private Tool_call tool_call;
    
    @Data
    @Accessors(chain = true)
    public static class Tool_call {
        private String id;
        private Map<String, Object> parameters;
    }
}
