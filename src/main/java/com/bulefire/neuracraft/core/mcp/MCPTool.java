package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface MCPTool {
    @NotNull String getName();
    @NotNull String getDescription();
    @NotNull MCPToolInfo getInfo();
    @NotNull String getPrompt();
    @NotNull MCPResponse execute(@NotNull MCPRequest request, @NotNull Consumer<Component> print);
    default boolean isValid() {
        return getInfo().getMethod() == null ||
                getInfo().getMethod().isEmpty() ||
                getInfo().getHost() == null ||
                getInfo().getType() == null;
    }
}