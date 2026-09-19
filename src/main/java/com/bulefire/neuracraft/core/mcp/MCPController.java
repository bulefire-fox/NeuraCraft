package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.scanner.AnnotationsMethodScanner;
import com.bulefire.neuracraft.core.command.GameCommand;
import com.bulefire.neuracraft.core.inside.model.openai.OpenAiTool;
import com.bulefire.neuracraft.core.inside.model.openai.OpenAiToolCall;
import com.bulefire.neuracraft.core.mcp.annotation.RegisterMCP;
import com.bulefire.neuracraft.core.mcp.command.MCPCommandRegister;
import com.bulefire.neuracraft.core.mcp.extern.ExternMCPController;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import com.bulefire.neuracraft.core.mcp.mssage.MCPError;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Log4j2
public class MCPController {
    private static final MCPController INSTANCE = new MCPController();
    private static final Gson GSON = new Gson();
    private static final long SERVER_THREAD_TIMEOUT_MS = 5000;
    
    private MCPController() {
        mcpManager = new MCPManager();
    }
    
    public static MCPController getInstance() {
        return INSTANCE;
    }

    @Getter
    private final MCPManager mcpManager;
    @Getter
    private final GameCommand GAME_COMMAND = GameCommand.getINSTANCE();
    
    @Getter
    private final Consumer<Component> emptyPrint = log::info;
    
    private boolean initialized;

    public synchronized void init() {
        if (initialized)
            return;
        initialized = true;
        log.info("MCPController init");
        // 扫描我们自己的MCP类
        var methods = AnnotationsMethodScanner.scanPackageToMethod("com.bulefire.neuracraft.core", Set.of(RegisterMCP.class));
        log.info("MCP found mcp {}", methods);
        
        for (Method method : methods) {
            try {
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Failed to invoke method , report to the mod develop, not NC!", e);
                throw new RuntimeException(e);
            }
        }
        
        // 注册命令
        MCPCommandRegister.buildCommands();
    }

    public void startExternal() {
        ExternMCPController.getInstance().start();
    }

    public void stopExternal() {
        ExternMCPController.getInstance().stop();
    }

    public @NotNull List<OpenAiTool> getOpenAiTools() {
        return mcpManager.getTools().stream()
                .map(this::toOpenAiTool)
                .toList();
    }

    /**
     * Execute a native OpenAI function call and return content for one or more
     * standard {@code role=tool} messages.
     */
    public @NotNull List<Content> processToolCall(
            @NotNull OpenAiToolCall toolCall,
            @NotNull Consumer<Component> print
    ) {
        String callId = toolCall.id() == null || toolCall.id().isBlank()
                ? UUID.randomUUID().toString()
                : toolCall.id();
        String functionName;
        try {
            functionName = toolCall.functionName();
        } catch (IllegalArgumentException e) {
            return List.of(Content.of("text", "[工具调用失败]: " + e.getMessage()));
        }

        Optional<MCPTool> otool = findToolByOpenAiName(functionName);
        if (otool.isEmpty()) {
            return List.of(Content.of("text", "[工具调用失败]: 工具不存在: " + functionName));
        }
        MCPTool tool = otool.get();

        final Map<String, Object> params;
        try {
            params = normalizeParameters(tool, parseArguments(toolCall.arguments()));
        } catch (IllegalArgumentException e) {
            return List.of(Content.of("text", "[工具调用失败]: " + e.getMessage()));
        }

        print.accept(
                Component.literal(tool.getDisplayName()).withStyle(
                        style -> style
                                .withColor(TextColor.fromRgb(0xFF69B4))
                                .withHoverEvent(new HoverEvent.ShowText(Component.translatable("neuracraft.mcp.command.list.hover.detail")))
                                .withClickEvent(new ClickEvent.RunCommand("/neuracraft mcp detail " + tool.getId().value()))
                )
        );

        MCPResponse response = executeTool(
                MCPRequest.builder()
                        .id(callId)
                        .name(tool.getId().value())
                        .params(params)
                        .build(),
                print
        );
        if (response.getResult().isError()) {
            return response.getResult().appendTextFirst("[工具调用失败] ").getContent();
        }
        return response.getResult().appendTextFirst("[工具调用成功] ").getContent();
    }
    
    /*
    input look like:
    {
      "tool_call": {
        "id": "此处填写与工具列表中完全一致的唯一调用ID",
        "parameters": {
          "参数1名称": "参数1值",
          "参数2名称": "参数2值"
        }
      }
    }
    
    out put look like:
    [工具调用结果]: details
     */
    public List<Content> processAgentInput(@NotNull String input, @NotNull Consumer<Component> print) {
        try {
            log.debug("catch agent input");
            MCPRequest request = MCPAgentInputParser.parse(input);
            log.debug("parse agent input to {}", request);
            MCPTool tool = mcpManager.getTool(request.getToolId());
            if (tool == null)
                return List.of(new Content("text", "[工具调用失败]: 工具不存在: " + request.getName()));
            print.accept(
                    Component.literal(tool.getDisplayName()).withStyle(
                            style -> style
                                    .withColor(TextColor.fromRgb(0xFF69B4))
                                    .withHoverEvent(new HoverEvent.ShowText(Component.translatable("neuracraft.mcp.command.list.hover.detail")))
                                    .withClickEvent(new ClickEvent.RunCommand("/neuracraft mcp detail " + request.getName()))
                    )
            );
            MCPResponse response = executeTool(request, print);
            if (response.getResult().isError())
                return response.getResult().appendTextFirst("[工具调用失败] ").getContent();
            return response.getResult().appendTextFirst("[工具调用成功] ").getContent();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid MCP agent input: {}", e.getMessage());
            return List.of(new Content("text", "[工具调用失败]: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("MCP tool call failed", e);
            return List.of(new Content("text", "[工具调用失败]: 内部错误."));
        }
    }

    public MCPResponse callTool(@NotNull MCPMessage message) {
        return callTool(message, emptyPrint);
    }

    public MCPResponse callTool(@NotNull MCPMessage message, @NotNull Consumer<Component> print) {
        log.debug("catch tool call");
        var request = MCPMessage.asRequest(message);
        return executeTool(request, print);
    }

    public MCPResponse callTool(@NotNull String toolName, @NotNull Map<String, Object> params) {
        return callTool(toolName, params, emptyPrint);
    }

    public MCPResponse callTool(
            @NotNull String toolName,
            @NotNull Map<String, Object> params,
            @NotNull Consumer<Component> print
    ) {
        return executeTool(
                MCPRequest.builder()
                        .id(UUID.randomUUID().toString())
                        .name(toolName)
                        .params(params)
                        .build(),
                print
        );
    }

    public MCPResponse callTool(@NotNull MCPRequest request, @NotNull Consumer<Component> print) {
        return executeTool(request, print);
    }

    private MCPResponse executeTool(@NotNull MCPRequest request, @NotNull Consumer<Component> print) {
        var tool = mcpManager.getTool(request.getToolId());
        log.debug("call {} ; found {}", request.getName(), tool);
        if (tool == null)
            return MCPMessage.responseBuilder()
                    .id(request.getId())
                    .result(MCPResponse.Result.of(new MCPError(MCPError.METHOD_NOT_FOUND, "工具不存在: " + request.getName())))
                    .build();
        
        return needServerThread(tool) ?
                onServerThread(tool.getDisplayName(), () -> invokeTool(tool, request, print)) :
                invokeTool(tool, request, print);
    }
    
    private MCPResponse invokeTool(MCPTool tool, MCPRequest request, Consumer<Component> print) {
        final MCPResponse response;
        try {
            response = tool.execute(request, print);
        } catch (RuntimeException e) {
            log.error("MCP tool execution failed for {}", request.getName(), e);
            return MCPMessage.responseBuilder()
                             .id(request.getId())
                             .result(MCPResponse.Result.of(new MCPError(MCPError.INTERNAL_ERROR, "工具执行失败")))
                             .build();
        }
        MCPResponse normalized = response.getId() == null ? response.withId(request.getId()) : response;
        return normalized.getResult() == null
                ? normalized.withResult(MCPResponse.Result.of(new MCPError(MCPError.INTERNAL_ERROR, "工具结果为空")))
                : normalized;
    }
    
    private boolean needServerThread(@NonNull MCPTool tool) {
        return switch (tool.getInfo().getType()) {
            case BUILT_IN, PLUGIN -> true;
            default -> false;
        };
    }
    
    private MCPResponse onServerThread(@NotNull String toolName, @NotNull Supplier<MCPResponse> action) {
        var server = CUtil.getServer.get();
        if (server == null) {
            return MCPMessage.responseBuilder()
                             .result(MCPResponse.Result.of(
                                     new MCPError(MCPError.INTERNAL_ERROR, "服务器不可用，工具未执行")))
                             .build();
        }
        
        if (server.isSameThread()) {
            return action.get();
        }
        
        CompletableFuture<MCPResponse> future = new CompletableFuture<>();
        
        try {
            server.execute(() -> {
                try {
                    future.complete(action.get());          // action 内部已吞掉 RuntimeException
                } catch (Throwable t) {
                    future.completeExceptionally(t);         // 兜住 Error 与意外 Throwable
                }
            });
        } catch (RejectedExecutionException e) {
            // 关服过程中主线程队列已关闭——CUtil.dispatchMessage 处理的同一个异常
            log.debug("Dropping tool {} because the server is shutting down", toolName);
            return MCPMessage.responseBuilder()
                             .result(MCPResponse.Result.of(
                                     new MCPError(MCPError.INTERNAL_ERROR, "服务器正在关闭，工具未执行")))
                             .build();
        }
        
        try {
            return future.get(SERVER_THREAD_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("Tool {} timed out waiting for the server thread", toolName);
            return MCPMessage.responseBuilder()
                             .result(MCPResponse.Result.of(
                                     new MCPError(MCPError.INTERNAL_ERROR, "等待服务器主线程超时，工具未执行")))
                             .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();              // 恢复中断标志
            return MCPMessage.responseBuilder()
                             .result(MCPResponse.Result.of(
                                     new MCPError(MCPError.INTERNAL_ERROR, "工具执行被中断")))
                             .build();
        } catch (ExecutionException e) {
            log.error("Tool {} failed on the server thread", toolName, e.getCause());
            return MCPMessage.responseBuilder()
                             .result(MCPResponse.Result.of(
                                     new MCPError(MCPError.INTERNAL_ERROR, "工具执行失败"+e.getCause())))
                             .build();
        }
    }

    private @NotNull OpenAiTool toOpenAiTool(@NotNull MCPTool tool) {
        MCPToolInfo info = tool.getInfo();
        Map<String, Object> schema = info.getRawSchema().isEmpty()
                ? buildSchema(info)
                : new LinkedHashMap<>(info.getRawSchema());
        return new OpenAiTool(
                "function",
                new OpenAiTool.Function(
                        toOpenAiFunctionName(tool.getId().value()),
                        tool.getDescription() + " MCP工具ID: " + tool.getId().value(),
                        schema
                )
        );
    }

    private @NotNull Map<String, Object> buildSchema(@NotNull MCPToolInfo info) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        info.getParams().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    properties.put(entry.getKey(), parameterSchema(entry.getValue()));
                    required.add(entry.getKey());
                });
        info.getOptional().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> properties.put(entry.getKey(), parameterSchema(entry.getValue())));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }

    private @NotNull Map<String, Object> parameterSchema(@NotNull MCPToolInfo.Param param) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", switch (param.type()) {
            case "int", "integer" -> "integer";
            case "float", "double", "number" -> "number";
            case "bool" -> "boolean";
            default -> param.type();
        });
        schema.put("description", param.describe() == null ? "" : param.describe());
        return schema;
    }

    private @NotNull Optional<MCPTool> findToolByOpenAiName(@NotNull String functionName) {
        MCPTool exact = mcpManager.getToolByName(functionName);
        if (exact != null) {
            return Optional.of(exact);
        }
        return mcpManager.getTools().stream()
                         .filter(tool -> toOpenAiFunctionName(tool.getId().value()).equals(functionName))
                         .findFirst();
    }

    private static @NotNull String toOpenAiFunctionName(@NotNull String toolId) {
        return toolId.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private static @NotNull Map<String, Object> parseArguments(@NotNull String arguments) {
        String normalized = arguments.isBlank() ? "{}" : arguments;
        try {
            var root = JsonParser.parseString(normalized);
            if (!root.isJsonObject()) {
                throw new IllegalArgumentException("工具参数必须是 JSON 对象");
            }
            Map<String, Object> parsed = GSON.fromJson(
                    root,
                    new TypeToken<Map<String, Object>>() {}.getType()
            );
            return parsed == null ? Collections.emptyMap() : parsed;
        } catch (JsonParseException | IllegalStateException e) {
            throw new IllegalArgumentException("工具参数 JSON 格式错误", e);
        }
    }

    private static @NotNull Map<String, Object> normalizeParameters(
            @NotNull MCPTool tool,
            @NotNull Map<String, Object> input
    ) {
        Map<String, Object> normalized = new LinkedHashMap<>(input);
        for (Map.Entry<String, MCPToolInfo.Param> entry : tool.getInfo().getParams().entrySet()) {
            Object value = normalized.get(entry.getKey());
            if (value instanceof Number number) {
                normalized.put(entry.getKey(), convertNumber(number, entry.getValue().javaType()));
            }
        }
        for (Map.Entry<String, MCPToolInfo.Param> entry : tool.getInfo().getOptional().entrySet()) {
            Object value = normalized.get(entry.getKey());
            if (value instanceof Number number) {
                normalized.put(entry.getKey(), convertNumber(number, entry.getValue().javaType()));
            }
        }
        return normalized;
    }

    private static Object convertNumber(@NotNull Number number, Class<?> targetType) {
        if (targetType == Integer.class || targetType == int.class) return number.intValue();
        if (targetType == Long.class || targetType == long.class) return number.longValue();
        if (targetType == Float.class || targetType == float.class) return number.floatValue();
        if (targetType == Double.class || targetType == double.class) return number.doubleValue();
        return number;
    }
}
