package com.bulefire.neuracraft.core.mcp;

import lombok.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MCPToolInfo {
    private Type type;
    private String name;
    @Builder.Default
    private Map<String, Param> params = new HashMap<>();
    @Builder.Default
    private Map<String, Param> optional = new HashMap<>();
    
    public record Param(String type, String describe, Class<?> javaType) {}
    
    public enum Type {
        BUILT_IN,
        PLUGIN,
        LOCAL,
        SSE,
        STREAMABLE_HTTP;
    }
}