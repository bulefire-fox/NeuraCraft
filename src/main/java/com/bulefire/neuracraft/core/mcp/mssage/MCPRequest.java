package com.bulefire.neuracraft.core.mcp.mssage;

import com.bulefire.neuracraft.core.mcp.ToolId;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable internal MCP request.
 */
public non-sealed class MCPRequest implements MCPMessage {
    @Getter
    private final String id;
    @Getter
    private final String name;
    private final Map<String, Object> params;

    public MCPRequest(String id, String name, Map<String, Object> params) {
        this.id = id;
        this.name = name;
        this.params = params == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(params));
    }
    
    public @NotNull ToolId getToolId() {
        return new ToolId(name);
    }

    public @NotNull Map<String, Object> getParams() {
        return params;
    }

    @Contract(" -> new")
    public static @NonNull MCPRequestBuilder builder() {
        return new MCPRequestBuilder();
    }

    public static final class MCPRequestBuilder {
        private String id;
        private String name;
        private Map<String, Object> params = Map.of();

        public MCPRequestBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MCPRequestBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MCPRequestBuilder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        @Contract(" -> new")
        public @NonNull MCPRequest build() {
            return new MCPRequest(id, name, params);
        }
    }

    @Override
    public String toString() {
        return "MCPRequest{id='%s', name='%s', params=%s}".formatted(id, name, params);
    }
}
