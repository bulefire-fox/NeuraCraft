package com.bulefire.neuracraft.core.agent.commnd;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class Show extends FullCommand.AbsCommand {
    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        feedback(context.getSource(), Component.translatable("neuracraft.command.show.use"));
        return 0;
    }
}
