package test.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

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
        
        System.out.println("Tools:");
        tools.tools().forEach(tool -> System.out.println(tool.name()));
        McpSchema.Tool tool = tools.tools().get(0);
        Map<String, Object> inputSchema = tool.inputSchema();
        System.out.println("Input schema:");
        for (Map.Entry<String, Object> entry : inputSchema.entrySet()) {
            String name = entry.getKey();
            System.out.println("name: " + name);
            Object value = entry.getValue();
            Map<String, Object> properties = (Map<String, Object>) value;
            System.out.println("properties: " + properties);
            System.out.println("properties entry : " + properties.entrySet());
            System.out.println("title: " + properties.get("title") + ", type: " + properties.get("type"));
        }
        System.out.println("Tools end");
        
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
                System.out.println(textContent.text());
            }
        });
        
        client.close();
    }
}
