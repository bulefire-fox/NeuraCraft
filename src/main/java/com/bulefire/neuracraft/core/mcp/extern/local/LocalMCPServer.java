package com.bulefire.neuracraft.core.mcp.extern.local;

import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.extern.AbsRemoteMCPServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.*;

/**
 * 表示本地 MCP server
 */
@Log4j2
public class LocalMCPServer extends AbsRemoteMCPServer {
    @Getter
    private final String name;
    private final String command;
    private final List<String> args;
    
    public LocalMCPServer(@NotNull String name, @NotNull String command, @NotNull List<String> args) {
        super(MCPToolInfo.Type.LOCAL);
        this.name = name;
        this.command = command;
        this.args = args;
    }
    
    /**
     * 启动 MCP server
     */
    @Override
    @SneakyThrows
    public void start() {
        ServerParameters params = ServerParameters.builder(command)
                                                  .args(args)
                                                  .build();
        McpClientTransport transport = new StdioClientTransport(params, new JacksonMcpJsonMapper(new ObjectMapper()));
        startClient(transport);
    }
    
    /**
     * 停止 本地 MCP server
     */
    @Override
    @SneakyThrows
    public void stop() {
        client.close();
    }
}
