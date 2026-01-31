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
    private URI host;
    private String method;
    @Builder.Default
    private Map<String, Param> params = new HashMap<>();
    @Builder.Default
    private Map<String, Param> optional = new HashMap<>();
    
    public static record Param(String type, String describe, Class<?> javaType) {}
    
    public enum Type {
        LOCAL,
        REMOTE;
        
        @Getter
        private final String head;
        
        Type() {
            head = "mcp:" + name().toLowerCase() + ":";
        }
    }
}
