package com.bulefire.neuracraft.core.mcp.command.admin;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.MCPTool;
import com.bulefire.neuracraft.core.mcp.MCPToolInfo;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;

import static com.bulefire.neuracraft.core.command.util.ComponentGenerator.withHoverAndCopy;

public class ListMCP extends FullCommand.AbsCommand {
    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MutableComponent component = Component.literal("");
        for (MCPTool mcp : MCPController.getInstance().getMcpManager().getTools()) {
            var details = Component.literal(" detail")
                                   .withStyle(style -> style
                                           .withColor(TextColor.parseColor("#FF69B4"))
                                           .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("neuracraft.mcp.command.list.hover.detail")))
                                           .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/neuracraft mcp detail "+mcp.getInfo().getName()))
                                   );
            component.append(Component.translatable(
                    "neuracraft.mcp.command.list.single",
                    withHoverAndCopy(mcp.getDisplayName(), "#7CFC00"),
                    withHoverAndCopy(mcp.getInfo().getName(), "#00ffff"),
                    details
            ));
        }
        feedback(context.getSource(), Component.translatable("neuracraft.mcp.command.list", MCPToolInfo.Type.LOCAL, component));
        return SINGLE_SUCCESS;
    }
}
