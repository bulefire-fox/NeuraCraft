package com.bulefire.neuracraft.core.mcp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable metadata shared by built-in, plugin and remote MCP tools.
 */
public final class MCPToolInfo {
    private final Type type;
    private final String name;
    private final String serverName;
    private final String originalName;
    private final Map<String, Object> rawSchema;
    private final Map<String, Param> params;
    private final Map<String, Param> optional;

    private MCPToolInfo(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.serverName = builder.serverName;
        this.originalName = builder.originalName;
        this.rawSchema = Collections.unmodifiableMap(new LinkedHashMap<>(builder.rawSchema));
        this.params = Collections.unmodifiableMap(new LinkedHashMap<>(builder.params));
        this.optional = Collections.unmodifiableMap(new LinkedHashMap<>(builder.optional));
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ToolId getToolId() {
        return new ToolId(name);
    }

    public String getServerName() {
        return serverName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public Map<String, Object> getRawSchema() {
        return rawSchema;
    }

    public Map<String, Param> getParams() {
        return params;
    }

    public Map<String, Param> getOptional() {
        return optional;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Type type;
        private String name;
        private String serverName = "";
        private String originalName = "";
        private Map<String, Object> rawSchema = Map.of();
        private Map<String, Param> params = Map.of();
        private Map<String, Param> optional = Map.of();

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName == null ? "" : serverName;
            return this;
        }

        public Builder originalName(String originalName) {
            this.originalName = originalName == null ? "" : originalName;
            return this;
        }

        public Builder rawSchema(Map<String, Object> rawSchema) {
            this.rawSchema = rawSchema == null ? Map.of() : rawSchema;
            return this;
        }

        public Builder params(Map<String, Param> params) {
            this.params = params == null ? Map.of() : params;
            return this;
        }

        public Builder optional(Map<String, Param> optional) {
            this.optional = optional == null ? Map.of() : optional;
            return this;
        }

        public MCPToolInfo build() {
            return new MCPToolInfo(this);
        }
    }

    public record Param(String type, String describe, Class<?> javaType) {}

    public enum Type {
        BUILT_IN,
        PLUGIN,
        LOCAL,
        SSE,
        STREAMABLE_HTTP
    }

    @Override
    public String toString() {
        return "MCPToolInfo{type=%s, name='%s', serverName='%s', originalName='%s'}"
                .formatted(type, name, serverName, originalName);
    }
}
