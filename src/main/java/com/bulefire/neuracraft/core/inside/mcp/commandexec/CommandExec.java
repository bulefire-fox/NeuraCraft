package com.bulefire.neuracraft.core.inside.mcp.commandexec;

import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.core.mcp.AbsMCPTool;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.bulefire.neuracraft.core.mcp.annotation.MCP;
import com.bulefire.neuracraft.core.mcp.annotation.RegisterMCP;
import com.bulefire.neuracraft.core.mcp.mssage.MCPError;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;

@MCP
@Log4j2
public class CommandExec extends AbsMCPTool {
    @RegisterMCP
    public static void init() {
        log.info("CommandExec init");
        MCPController.getInstance().getMcpManager().registerTool(new CommandExec());
    }
    
    public CommandExec() {
        super(
                "命令执行器",
                "执行minecraft命令",
                MCPToolInfo.builder()
                           .type(MCPToolInfo.Type.LOCAL)
                           .host(URI.create(MCPToolInfo.Type.LOCAL.getHead() + "command_exec"))
                            .method("tool.game.command_exec")
                           .params(Map.of("command", new MCPToolInfo.Param("string", "要在minecraft服务器中执行的命令", String.class)))
                           .build()
        );
    }
    
    @Override
    public @NotNull MCPResponse execute(@NotNull MCPRequest request) {
        MinecraftServer server = CUtil.getServer.get();
        if (! request.getParams().containsKey("command"))
            return MCPMessage.responseFailedBuilder()
                             .id(request.getId())
                             .error(new MCPError(MCPError.INVALID_REQUEST, "command is null", null))
                             .build();
        if (request.getParams().get("command") instanceof String command) {
            int number = server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(),
                    command
            );
            if (number <= 0) {
                // 失败
                return MCPMessage.responseFailedBuilder()
                                 .id(request.getId())
                                 .error(new MCPError(MCPError.INVALID_PARAMS, "command is not valid", null))
                                 .build();
                
            }
            // 成功
            return MCPMessage.responseSuccessBuilder()
                             .id(request.getId())
                             .result("success exec "+ number+ " command")
                             .build();
        } else {
            log.debug("command is not a string is {}", request.getParams().get("command").getClass());
            return MCPMessage.responseFailedBuilder()
                            .id(request.getId())
                            .error(new MCPError(MCPError.INVALID_PARAMS, "command is not a string", null))
                            .build();
        }
    }
}
