package com.bulefire.neuracraft.core.agent.commnd.self;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.extern.log4j.Log4j2;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@Log4j2
public class Find extends FullCommand.AbsCommand {
    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        String playerName = Objects.requireNonNull(commandContext.getSource().getPlayer()).getName().getString();
        UUID playerUUID = Objects.requireNonNull(commandContext.getSource().getPlayer()).getUUID();

        var player = new APlayer(playerName, playerUUID);
        var agentManager = AgentController.getAgentManager();
        var playerManager = AgentController.getPlayerManager();
        var agent = agentManager.getAgent(playerManager.getPlayerAgentUUID(player));

        if (agent == null) {
            feedback(commandContext.getSource(), Component.translatable("neuracraft.command.find.notInChatRoom"));
            return 1;
        }

        feedback(commandContext.getSource(), Component.translatable("neuracraft.command.find.success", agent.getName(), agent.getUUID()));
        log.info("player {} in Agent: {} UUID: {}", playerName, agent.getName(), agent.getUUID());
        return 1;
    }
}
