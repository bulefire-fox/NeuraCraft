package com.bulefire.neuracraft.core.mcp.extern.sse;

import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.extern.AbsRemoteMCPServer;
import com.bulefire.neuracraft.core.mcp.extern.RemoteMCPServer;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

@Log4j2
public class SSEMCPServer extends AbsRemoteMCPServer {
    private final String url;
    
    public SSEMCPServer(@NotNull String name, @NotNull String url) {
        super(name, MCPToolInfo.Type.SSE);
        this.url = url;
    }
    
    @Override
    public void start() {
        log.info("staring sse mcp server: {} url {}", getName(), url);
        McpClientTransport transport = HttpClientSseClientTransport
                .builder(url)
                .jsonMapper(McpJsonMapper.getDefault())
                .build();
        log.info("transport: {}", transport);
        startClient(transport);
    }
    
    @Override
    public void stop() {
        try {
            if (client != null)
                client.close();
        } catch (Exception e) {
            log.warn("Error closing MCP client", e);
        }
    }
}
