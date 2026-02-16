package com.bulefire.neuracraft.core.mcp.command.control;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.MCPTool;
import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class Detail extends FullCommand.AbsCommand {
    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String method = context.getArgument("method", String.class);
        MCPTool tool = MCPController.getInstance().getMcpManager().getToolByName(method);
        if (tool == null) {
            feedback(context.getSource(), Component.translatable("neuracraft.mcp.command.detail.not_found", method));
            return SINGLE_SUCCESS;
        }
        MCPToolInfo info = tool.getInfo();
        feedback(
                context.getSource(), Component.translatable(
                        "neuracraft.mcp.command.detail",
                        tool.getDisplayName(),
                        tool.getDisplayName(),
                        info.getName(),
                        tool.getDescription(),
                        tool.getPrompt(),
                        info.getParams(),
                        info.getOptional(),
                        info.getType()
                )
        );
        return SINGLE_SUCCESS;
    }
}
