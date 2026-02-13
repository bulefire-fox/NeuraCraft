package com.bulefire.neuracraft.core.mcp.extern.streamablehttp;

import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.extern.AbsRemoteMCPServer;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;

@Log4j2
public class StreamableHttpMCPServer extends AbsRemoteMCPServer {
    private final String url;
    
    public StreamableHttpMCPServer(String name, String url) {
        super(name, MCPToolInfo.Type.STREAMABLE_HTTP);
        this.url = url;
    }
    
    @Override
    public void start() {
        log.info("streamable http mcp server mcp server: {} url {}", getName(), url);
        McpClientTransport transport = HttpClientSseClientTransport
                .builder(url)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
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
