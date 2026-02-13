package test.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Log4j2
public class TestClient {
    public static void main(String[] args) throws IOException {
        ServerParameters params = ServerParameters.builder("python")
                                                  .args("E:/files/project/NeuraCraft/NeuraCraft-main/src/test/mcp_server/main.py")
                                                  .build();
        McpClientTransport transport = new StdioClientTransport(params, new JacksonMcpJsonMapper(new ObjectMapper()));
        
        McpSyncClient client = McpClient.sync(transport)
                                        .requestTimeout(Duration.ofSeconds(5))
                                        .capabilities(
                                                McpSchema.ClientCapabilities
                                                        .builder()
                                                        .roots(true)
                                                        .build()
                                        )
                                        .build();
        
        client.initialize();
        
        McpSchema.ListToolsResult tools = client.listTools();
        
        log.info("Tools:");
        tools.tools().forEach(tool -> log.info(tool.name()));
        McpSchema.Tool tool = tools.tools().get(0);
        McpSchema.JsonSchema inputSchema = tool.inputSchema();
        log.info("Input schema:");
        for (Map.Entry<String, Object> entry : inputSchema.properties().entrySet()) {
            String name = entry.getKey();
            log.info("name: {}", name);
            Object value = entry.getValue();
            Map<String, Object> properties = (Map<String, Object>) value;
            log.info("properties: {}",properties);
            log.info("properties entry : {}",properties.entrySet());
            log.info("title: {}, type: {}", properties.get("title"), (String) properties.get("type"));
        }
        log.info("Tools end");
        
        McpSchema.CallToolResult result = client.callTool(
                McpSchema.CallToolRequest
                        .builder()
                        .name("add")
                        .arguments(Map.of("a", 1, "b", 2))
                        .build()
        );
        
        result.content().forEach(content -> {
            if (content.type().equals("text")) {
                McpSchema.TextContent textContent = (McpSchema.TextContent) content;
                log.info(textContent.text());
            }
        });
        
        client.close();
    }
}
