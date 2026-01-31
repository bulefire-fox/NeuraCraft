package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import org.jetbrains.annotations.NotNull;

public interface MCPTool {
    @NotNull String getName();
    @NotNull String getDescription();
    @NotNull MCPToolInfo getInfo();
    @NotNull String getPrompt();
    @NotNull MCPResponse execute(@NotNull MCPRequest request);
}
