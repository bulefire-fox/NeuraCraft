package com.bulefire.neuracraft.core.mcp;

import org.jetbrains.annotations.NotNull;

/**
 * Stable identity of an MCP tool.
 */
public record ToolId(@NotNull String value) {
    public ToolId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tool ID must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
