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
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Log4j2
public abstract class AbsRemoteMCPServer implements RemoteMCPServer {
    private final MCPToolInfo.Type type;
    protected McpSyncClient client;
    private ExecutorService executor;
    
    protected AbsRemoteMCPServer(MCPToolInfo.Type type) {
        log.debug("init remote mcp server");
        this.type = type;
        log.debug("init end");
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
        tools.tools().stream().map(this::buildMCPTool).forEach(tool -> MCPController.getInstance().getMcpManager().registerTool(tool));
    }
    
    protected @NotNull MCPTool buildMCPTool(McpSchema.@NotNull Tool tool) {
        String name = tool.name();
        String description = tool.description();
        Map<String, MCPToolInfo.Param> params = new HashMap<>();
        Map<String, MCPToolInfo.Param> optional = new HashMap<>();
        Map<String, Object> properties = tool.inputSchema().properties();
        List<String> require = tool.inputSchema().required();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String paramsName = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> iParams = (Map<String, Object>) entry.getValue();
            String title = (String) iParams.get("title");
            String type = (String) iParams.get("type");
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
                           .name("tool.extern.local" + name.replace("/", "."))
                           .params(params)
                           .optional(optional)
                           .build(),
                (request, print) -> {
                    var result = client.callTool(
                            McpSchema.CallToolRequest
                                    .builder()
                                    .name(name)
                                    .arguments(request.getParams())
                                    .build()
                    );
                    var content = result.content().stream()
                          .filter(c -> c.type().equals("text"))
                          .map(c -> (McpSchema.TextContent) c)
                          .map(c -> new Content("text", c.text()))
                          .toList();
                    return MCPMessage.responseBuilder()
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
