package com.bulefire.neuracraft.core.mcp.extern.local;

import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.extern.AbsRemoteMCPServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import lombok.Getter;
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
        log.debug("init local mcp server");
        this.name = name;
        this.command = command;
        this.args = args;
        log.debug("init end");
    }
    
    /**
     * 启动 MCP server
     */
    @Override
    public void start() {
        log.info("local mcp server: {} command {} arg {}", name, command, args);
        try {
            ServerParameters params = ServerParameters.builder(command)
                                                      .args(args)
                                                      .build();
            ObjectMapper objectMapper = new ObjectMapper();
            Class<?> jacksonMapperClass = Class.forName("io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper");
            Object jacksonMapper = jacksonMapperClass.getConstructor(ObjectMapper.class).newInstance(objectMapper);

            McpClientTransport transport = new StdioClientTransport(params, (McpJsonMapper) jacksonMapper);
            startClient(transport);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MCP class not found: " + e.getMessage() +
                    ". Ensure implementation('io.modelcontextprotocol.sdk:mcp-*') in build.gradle.", e);
        } catch (NoClassDefFoundError e) {
            throw new RuntimeException("MCP class not found: " + e.getMessage() +
                    ". Run 'gradlew build' or refresh Gradle.", e);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to start MCP server " + name, e);
        }
    }
    
    /**
     * 停止 本地 MCP server
     */
    @Override
    public void stop() {
        try {
            if (client != null) client.close();
        } catch (Exception e) {
            log.warn("Error closing MCP client", e);
        }
    }
}
