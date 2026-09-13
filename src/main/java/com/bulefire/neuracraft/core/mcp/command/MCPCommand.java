package com.bulefire.neuracraft.core.mcp.command;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class MCPCommand extends FullCommand.AbsCommand {
    private static final Logger logger = LogUtils.getLogger();

    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        feedback(context.getSource(), Component.translatable("neuracraft.mcp.hello"));
        return 1;
    }
}
