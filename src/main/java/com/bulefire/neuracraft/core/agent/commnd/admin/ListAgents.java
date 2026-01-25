package com.bulefire.neuracraft.core.agent.commnd.admin;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ListAgents extends FullCommand.AbsCommand {
    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        feedback(commandContext.getSource(), Component.translatable("neuracraft.command.list.chatroom", AgentController.getAgentManager().getAllAgents()));
        return 1;
    }
}
