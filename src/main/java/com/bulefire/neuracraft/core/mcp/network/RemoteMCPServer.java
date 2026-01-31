package com.bulefire.neuracraft.core.mcp.network;

import com.bulefire.neuracraft.core.mcp.MCPTool;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RemoteMCPServer {
    void initialize();
    @NotNull List<MCPTool> getAliveTools();
    @NotNull MCPTool getTool(@NotNull String name);
    @NotNull MCPResponse call(@NotNull MCPRequest request);
    boolean isAlive();
}
