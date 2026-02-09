package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * MCP工具接口
 * 仅用于Java内部的简化表示，与外部MCP工具接口不同
 */
public interface MCPTool {
    @NotNull String getDisplayName();
    @NotNull String getDescription();
    @NotNull MCPToolInfo getInfo();
    @NotNull String getPrompt();
    @NotNull MCPResponse execute(@NotNull MCPRequest request, @NotNull Consumer<Component> print);
    default boolean isValid() {
        return getInfo().getName() == null ||
                getInfo().getName().isEmpty() ||
                getInfo().getType() == null;
    }
}