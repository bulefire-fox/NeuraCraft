package com.bulefire.neuracraft.core.mcp.command;

import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.command.admin.ListMCP;
import com.bulefire.neuracraft.core.mcp.command.control.CallTool;
import com.bulefire.neuracraft.core.mcp.command.control.Detail;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;

public class MCPCommandRegister {
    public static void buildCommands() {
        MCPController.getInstance().getGAME_COMMAND().getMcpBaseCommand()
                     .then(Commands.literal("list")
                                   .executes(new ListMCP())
                     )
                     .then(Commands.literal("call")
                                   .then(Commands.argument("method", StringArgumentType.string())
                                                 .then(Commands.argument("param", StringArgumentType.string())
                                                               .executes(new CallTool())
                                                 )
                                   )
                     )
                     .then(Commands.literal("detail")
                                   .then(Commands.argument("method", StringArgumentType.word())
                                                 .executes(new Detail())
                                   )
                     );
    }
}
