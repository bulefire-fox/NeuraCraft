package com.bulefire.neuracraft.core.mcp.extern;

import com.bulefire.neuracraft.core.mcp.AbsMCPTool;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.MCPTool;
import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.mssage.MCPError;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import com.google.gson.Gson;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Log4j2
public abstract class AbsRemoteMCPServer implements RemoteMCPServer{
    
    protected static final String initializeJson = """
            {
              "jsonrpc": "2.0",
              "id": "%s",
              "method": "initialize",
              "params": {
                "protocolVersion": "%s",
                "capabilities": {
                  "tools": {},
                  "resources": {},
                  "prompts": {}
                },
                "clientInfo": {
                  "name": "%s",
                  "version": "%s"
                }
              }
            }
            """;
    
    
    protected static final String listJson = """
            {
              "jsonrpc": "2.0",
              "id": "%s",
              "method": "tools/list",
              "params": {}
            }
            """;
    
    protected void init(){
        // 构造初始化json字符串
        String json = initializeJson.formatted("initialize" + ThreadLocalRandom.current().nextInt(), "2024-11-05", "NeuraCraft", "1.0");
        // 忽略结果,因为不会出错(但愿)
        send(json);
    }
    
    protected void registerAllTool() {
        registerAllTool(new HashMap<>(), listTools());
    }
    
    protected void registerAllTool(@NotNull Map<String, MCPTool> stone) {
        registerAllTool(stone, listTools());
    }
    
    protected void registerAllTool(@NotNull Map<String, MCPTool> stone, @NotNull List<ListResult.Result.ToolsBean> listTools) {
        for (var tool : listTools) {
            // 注册包装器
            var mcp = buildMCPTool(tool);
            MCPController.getInstance().getMcpManager().registerTool(mcp);
            stone.put(mcp.getName(), mcp);
        }
    }
    
    protected List<ListResult.Result.ToolsBean> listTools() {// 获取工具列表
        String rj = send(listJson.formatted("list-" + ThreadLocalRandom.current().nextInt()));
        log.debug("list result: {}", rj);
        Gson gson = new Gson();
        ListResult listResult = gson.fromJson(rj, ListResult.class);
        return listResult.result.tools;
    }
    
    protected MCPTool buildMCPTool(ListResult.Result.ToolsBean tool){
        log.debug("Tool: {}", tool);
        String name = tool.getName();
        String description = tool.getDescription();
        
        var inputSchema = tool.inputSchema;
        var params = new HashMap<String, MCPToolInfo.Param>();
        var optional = new HashMap<String, MCPToolInfo.Param>();
        // 构造参数列表
        for (var param : inputSchema.getProperties().entrySet()) {
            String key = param.getKey();
            String title = param.getValue().title;
            String type = param.getValue().type;
            // 获取对应的 java type
            Class<?> javaType = switch (type) {
                case "string" -> String.class;
                case "int", "integer" -> Integer.class;
                case "float" -> Float.class;
                case "double" -> Double.class;
                case "long" -> Long.class;
                case "boolean" -> Boolean.class;
                default -> null;
            };
            if (inputSchema.getRequired().contains(key))
                // 必须参数
                params.put(key, new MCPToolInfo.Param(type, title, javaType));
            else
                // 可选参数
                optional.put(key, new MCPToolInfo.Param(type, title, javaType));
        }
        // 构造info表示
        MCPToolInfo info = MCPToolInfo.builder()
                                      .type(MCPToolInfo.Type.REMOTE)
                                      .host(URI.create(MCPToolInfo.Type.REMOTE.getHead() + name))
                                      .method("tool.extern." + name
                                              .replace("/", ".")
                                              .replace("_", ".")
                                              .replace(" ", ""))
                                      .params(params)
                                      .optional(optional)
                                      .build();
        log.debug("Info: {}", info);
        // 构造 内部的 MCPTool表示
        return new RMCPTool(
                name,
                description,
                info,
                // 真正执行的函数
                (request, print) -> {
                    var iParams = request.getParams();
                    // 超级无敌循环!
                    // 我们只能遍历获取参数
                    for (var param : info.getParams().entrySet()) {
                        if (! iParams.containsKey(param.getKey()))
                            // 缺少参数
                            return MCPMessage.responseFailedBuilder()
                                             .id(request.getId())
                                             .error(new MCPError(MCPError.INVALID_REQUEST, "Missing param: " + param.getKey()))
                                             .build();
                    }
                    
                    // 构造标准MCP通信的jsonrpc字符串的参数列表
                    StringBuilder sb = new StringBuilder();
                    for (var param : iParams.entrySet()) {
                        sb.append("\"")
                          .append(param.getKey())
                          .append("\"")
                          .append(":")
                          .append("\"")
                          .append(param.getValue())
                          .append("\"")
                          .append(",");
                    }
                    // 删除尾随逗号
                    sb.deleteCharAt(sb.length() - 1);
                    // 模板字符串
                    String iJson = """
                                {
                                "jsonrpc":"2.0",
                                "id":"%s",
                                "method":"tools/call",
                                "params":{
                                    "name":"%s",
                                    "arguments":{
                                            %s
                                        }
                                    }
                                }
                                """.formatted(ThreadLocalRandom.current().nextInt(), name, sb.toString());
                    Gson iGson = new Gson();
                    // 获取结果
                    Response response = iGson.fromJson(send(iJson), Response.class);
                    if (response.result.isError)
                        // 返回错误,返回错误消息
                        return MCPMessage.responseFailedBuilder()
                                         .id(request.getId())
                                         .error(new MCPError(MCPError.INVALID_PARAMS, response.result.content.get(0).text, null))
                                         .build();
                    // 返回内部的msg
                    return MCPMessage.responseSuccessBuilder()
                                     .id(request.getId())
                                     .result("[调用结果]: " + response.result.content.get(0).text)
                                     .build();
                }
        );
    }
    
    @Accessors(chain=true)
    @Data
    @ToString
    private static class ListResult {
        private String jsonrpc;
        private String id;
        private Result result;
        
        @Accessors(chain=true)
        @Data
        @ToString
        public static class Result {
            private List<ToolsBean> tools;
            
            @Accessors(chain=true)
            @Data
            @ToString
            public static class ToolsBean {
                private String name;
                private String description;
                private InputSchema inputSchema;
                
                @Accessors(chain=true)
                @Data
                @ToString
                public static class InputSchema {
                    private Map<String, Param> properties;
                    private List<String> required;
                    private String title;
                    private String type;
                    
                    @Accessors(chain=true)
                    @Data
                    @ToString
                    public static class Param {
                        private String title;
                        private String type;
                    }
                }
            }
        }
    }
    
    //{"jsonrpc":"2.0","id":"1","result":{"content":[{"type":"text","text":"3"}],"isError":false}}
    @Data
    @ToString
    private static class Response {
        private String jsonrpc;
        private String id;
        private Result result;
        
        @Data
        public static class Result {
            private List<ContentBean> content;
            private boolean isError;
            
            @Data
            public static class ContentBean {
                private String type;
                private String text;
            }
        }
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
