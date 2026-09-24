package com.bulefire.neuracraft.core.mcp.extern;

import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.core.mcp.AbsMCPTool;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.MCPTool;
import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Log4j2
public abstract class AbsRemoteMCPServer implements RemoteMCPServer {
    @Getter
    private final String name;
    private final MCPToolInfo.Type type;
    protected McpSyncClient client;
    private ExecutorService executor;
    
    protected AbsRemoteMCPServer(@NotNull String name, @NotNull MCPToolInfo.Type type) {
        this.name = name;
        this.type = type;
    }
    
    protected void startClient(McpClientTransport transport) {
        client = McpClient.sync(transport)
                          .requestTimeout(Duration.ofSeconds(5))
                          .capabilities(
                                  McpSchema.ClientCapabilities.builder()
                                                              .roots(true)
                                                              .sampling()
                                                              .build()
                          )
                          .build();
        
        client.initialize();
        log.debug("Client: {}", client);
        
        McpSchema.ListToolsResult tools = client.listTools();
        tools.tools().stream().map(this::buildMCPTool).forEach(tool -> {
            MCPController.getInstance().getMcpManager().registerTool(tool);
            com.bulefire.neuracraft.core.agent.AgentController.refreshPrompts();
        });
    }
    
    protected @NotNull MCPTool buildMCPTool(McpSchema.@NotNull Tool tool) {
        String name = tool.name();
        String description = tool.description() == null ? "" : tool.description();
        Map<String, MCPToolInfo.Param> params = new HashMap<>();
        Map<String, MCPToolInfo.Param> optional = new HashMap<>();
        Map<String, Object> inputSchema = tool.inputSchema() == null ? Collections.emptyMap() : tool.inputSchema();
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = inputSchema.get("properties") instanceof Map<?, ?> value
                ? (Map<String, Object>) value
                : Collections.emptyMap();
        List<String> require = inputSchema.get("required") instanceof List<?> value
                ? value.stream().filter(String.class::isInstance).map(String.class::cast).toList()
                : Collections.emptyList();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String paramsName = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> iParams = entry.getValue() instanceof Map<?, ?> value
                    ? (Map<String, Object>) value
                    : Collections.emptyMap();
            String title = iParams.get("description") instanceof String value ? value
                    : iParams.get("title") instanceof String value ? value : paramsName;
            String type = iParams.get("type") instanceof String value ? value : "object";
            var info = new MCPToolInfo.Param(type, title, getJavaType(type));
            if (require.contains(paramsName))
                params.put(paramsName, info);
            else
                optional.put(paramsName, info);
        }
        return new RMCPTool(
                name, description,
                MCPToolInfo.builder()
                           .type(type)
                           .serverName(getName())
                           .originalName(name)
                           .rawSchema(inputSchema)
                           .name("tool.extern."+type.toString().toLowerCase()+"."+name.replace("/", "."))
                           .params(params)
                           .optional(optional)
                           .build(),
                (request, print) -> {
                    var result = client.callTool(
                            McpSchema.CallToolRequest.builder(name)
                                                     .arguments(request.getParams())
                                                     .build()
                    );
                    var content = result.content().stream()
                          .filter(c -> c.type().equals("text"))
                          .map(c -> (McpSchema.TextContent) c)
                          .map(c -> new Content("text", c.text()))
                          .toList();
                    return MCPMessage.responseBuilder()
                                     .id(request.getId())
                                     .result(new MCPResponse.Result(content, result.isError()))
                                     .build();
                }
        );
    }
    
    private @NotNull Class<?> getJavaType(@NotNull String type) {
        return switch (type) {
            case "string" -> String.class;
            case "number", "double" -> Double.class;
            case "integer" -> Integer.class;
            case "float" -> Float.class;
            case "boolean" -> Boolean.class;
            default -> Object.class;
        };
    }
    
    @ToString
    private static class RMCPTool extends AbsMCPTool {
        private final BiFunction<MCPRequest, Consumer<Component>, MCPResponse> run;
        
        public RMCPTool(String name, String description, MCPToolInfo info, BiFunction<MCPRequest, Consumer<Component>, MCPResponse> run) {
            super(name, description, info);
            this.run = run;
        }
        
        @Override
        public @NotNull MCPResponse execute(@NotNull MCPRequest request, @NotNull Consumer<Component> print) {
            return run.apply(request, print);
        }
    }
}
