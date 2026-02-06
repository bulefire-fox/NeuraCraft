package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.compatibility.util.scanner.AnnotationsMethodScanner;
import com.bulefire.neuracraft.core.command.GameCommand;
import com.bulefire.neuracraft.core.mcp.annotation.RegisterMCP;
import com.bulefire.neuracraft.core.mcp.command.MCPCommandRegister;
import com.bulefire.neuracraft.core.mcp.entity.AgentInput;
import com.bulefire.neuracraft.core.mcp.mssage.MCPError;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Log4j2
public class MCPController {
    private static final MCPController INSTANCE = new MCPController();
    
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
    
    public void init() {
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
        
        // TODO:从配置文件加载远程 MCP 服务器
        
        // 初始化远程 MCP 服务器
        //RemoteMCPServerController.getInstance().initializeAllRemoteServer();
        
        // 注册命令
        MCPCommandRegister.buildCommands();
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
    public String processAgentInput(@NotNull String input, @NotNull Consumer<Component> print) {
        log.debug("catch agent input");
        MCPRequest request = parseAgentInput(input);
        log.debug("parse agent input to {}", request);
        print.accept(
                Component.literal(
                    Objects.requireNonNull(mcpManager.getToolByMethod(request.getMethod())).getName()
                ).withStyle(
                        style -> style
                                .withColor(TextColor.parseColor("#FF69B4"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("neuracraft.mcp.command.list.hover.detail")))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neuracraft mcp detail " + request.getMethod()))
                )
        );
        MCPResponse response;
        try {
            response = callTool(request);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        if (! Objects.equals(request.getId(), response.getId())) return "[工具调用失败]: 调用丢失,请重试.";
        if (response instanceof MCPResponse.Failed failed) {
            var error = failed.getError();
            return "[工具调用失败]: " + error.getCode() + " " + MCPError.getCodeString(error.getCode()) +" "+ error.getMessage();
        }
        MCPResponse.Success success = (MCPResponse.Success) response;
        return "[工具调用成功]: " + success.getResult();
    }
    
    private @NotNull MCPRequest parseAgentInput(@NotNull String input) {
        AgentInput agentInput = MCPMessage.gobalGson.fromJson(input, AgentInput.class);
        return MCPMessage.requestBuilder()
                .id(String.valueOf(System.currentTimeMillis()))
                .method(agentInput.getTool_call().getId())
                .params(agentInput.getTool_call().getParameters())
                .build();
    }
    
    public MCPResponse callTool(@NotNull MCPMessage message) {
        return callTool(message, emptyPrint);
    }
    
    public MCPResponse callTool(@NotNull MCPMessage message, @NotNull Consumer<Component> print) {
        log.debug("catch tool call");
        var request = MCPMessage.asRequest(Objects.requireNonNull(message));
        if (! request.getJsonrpc().equals("2.0"))
            return MCPMessage.responseFailedBuilder()
                    .id(request.getId())
                    .error(new MCPError(MCPError.INTERNAL_ERROR, "jsonrpc is not 2.0", null))
                    .build();
        var tool = mcpManager.getToolByMethod(request.getMethod());
        log.debug("call {} ; found {}", request.getMethod(), tool);
        if (tool == null)
            return MCPMessage.responseFailedBuilder()
                    .id(request.getId())
                    .error(new MCPError(MCPError.METHOD_NOT_FOUND, "tool %s not found".formatted(request.getMethod()), null))
                    .build();
        return tool.execute(request, print);
    }
}
