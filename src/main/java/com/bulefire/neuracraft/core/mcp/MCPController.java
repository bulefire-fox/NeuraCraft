package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.compatibility.util.scanner.AnnotationsMethodScanner;
import com.bulefire.neuracraft.core.command.GameCommand;
import com.bulefire.neuracraft.core.mcp.annotation.RegisterMCP;
import com.bulefire.neuracraft.core.mcp.command.MCPCommandRegister;
import com.bulefire.neuracraft.core.mcp.entity.AgentInput;
import com.bulefire.neuracraft.core.mcp.extern.ExternMCPController;
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
import java.util.List;
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
    private final ExternMCPController extern = ExternMCPController.getInstance();
    
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
        extern.init();
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
    public List<Content> processAgentInput(@NotNull String input, @NotNull Consumer<Component> print) {
        log.debug("catch agent input");
        MCPRequest request = parseAgentInput(input);
        log.debug("parse agent input to {}", request);
        print.accept(
                Component.literal(
                    Objects.requireNonNull(mcpManager.getToolByName(request.getName())).getDisplayName()
                ).withStyle(
                        style -> style
                                .withColor(TextColor.parseColor("#FF69B4"))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("neuracraft.mcp.command.list.hover.detail")))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neuracraft mcp detail " + request.getName()))
                )
        );
        MCPResponse response;
        try {
            response = callTool(request, print);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        if (! Objects.equals(request.getId(), response.getId())) return List.of(new Content("text","[工具调用失败]: 调用丢失,请重试."));
        if (response.getResult().isError()) {
            return response.getResult().appendTextFirst("[工具调用失败] ").getContent();
        }
        return response.getResult().appendTextFirst("[工具调用成功] ").getContent();
    }
    
    private @NotNull MCPRequest parseAgentInput(@NotNull String input) {
        AgentInput agentInput = MCPMessage.gobalGson.fromJson(input, AgentInput.class);
        return MCPMessage.requestBuilder()
                .id(String.valueOf(System.currentTimeMillis()))
                .name(agentInput.getTool_call().getId())
                .params(agentInput.getTool_call().getParameters())
                .build();
    }
    
    public MCPResponse callTool(@NotNull MCPMessage message) {
        return callTool(message, emptyPrint);
    }
    
    public MCPResponse callTool(@NotNull MCPMessage message, @NotNull Consumer<Component> print) {
        log.debug("catch tool call");
        var request = MCPMessage.asRequest(Objects.requireNonNull(message));
        var tool = mcpManager.getToolByName(request.getName());
        log.debug("call {} ; found {}", request.getName(), tool);
        if (tool == null)
            return MCPMessage.responseBuilder()
                    .id(request.getId())
                    .result(
                            MCPResponse.Result
                                    .builder()
                                    .content(List.of(new Content("txt", "工具不存在")))
                                    .isError(true)
                                    .build()
                    )
                    .build();
        return tool.execute(request, print);
    }
}
