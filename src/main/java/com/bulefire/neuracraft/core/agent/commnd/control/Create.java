package com.bulefire.neuracraft.core.agent.commnd.control;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.PlayerJoinEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.core.agent.Agent;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.util.NoAgentFound;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.extern.log4j.Log4j2;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;

import java.util.Objects;
import java.util.UUID;

@Log4j2
public class Create extends FullCommand.AbsCommand {
    private final boolean greet;

    public Create(boolean greet) {
        this.greet = greet;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        String agentName = StringArgumentType.getString(commandContext, "agentName");
        String sModel = StringArgumentType.getString(commandContext, "chatModel");
        String playerName = Objects.requireNonNull(commandContext.getSource().getPlayer()).getName().getString();
        UUID playerUUID = commandContext.getSource().getPlayer().getUUID();
        var agentManger = AgentController.getInstance().getAgentManager();
        var playerManager = AgentController.getInstance().getPlayerManager();

        Agent agent;
        try {
            agent = agentManger.createAgent(sModel);
        } catch (NoAgentFound e) {
            feedback(commandContext.getSource(), Component.translatable("neuracraft.agent.command.create.failure.unknownModel", sModel, agentManger.getAllAliveAgentKeys()));
            return 1;
        }

        agent.setName(agentName);
        APlayer player = new APlayer(playerName, playerUUID);
        agent.addPlayer(player);
        agent.addAdmin(player);
        playerManager.updatePlayer(player, agent.getUUID());
        try {
            agent.saveToFile(AgentController.getAgentPath(agent));
        } catch (Exception e) {
            playerManager.removePlayer(player);
            agentManger.removeAgent(agent.getUUID());
            log.error("Failed to save newly created agent {}", agent.getUUID(), e);
            feedback(commandContext.getSource(), Component.translatable("neuracraft.agent.command.create.failure.unknown"));
            return 1;
        }
        var name = Component.literal(agent.getName())
                 .withStyle(style -> style
                         .withColor(TextColor.fromRgb(0x7CFC00))
                         .withHoverEvent(new HoverEvent.ShowText(Component.translatable("neuracraft.agent.command.hover.copy_to_clipboard")))
                         .withClickEvent(new ClickEvent.CopyToClipboard(agent.getName()))
                 );
        var uuid = Component.literal(agent.getUUID().toString())
                 .withStyle(style -> style
                         .withColor(TextColor.fromRgb(0x00FFFF))
                         .withHoverEvent(new HoverEvent.ShowText(Component.translatable("neuracraft.agent.command.hover.copy_to_clipboard")))
                         .withClickEvent(new ClickEvent.CopyToClipboard(agent.getUUID().toString()))
                 );
        feedback(commandContext.getSource(), Component.translatable("neuracraft.agent.command.create.success", name, uuid));
        if (greet) {
            var server = CUtil.getServer.get();
            ChatEventProcesser.ChatMessage.Env env = CUtil.getEnv(server);
            PlayerJoinEventProcesser.onPlayerJoin(new PlayerJoinEventProcesser.JoinMessage(player, env));
        }
        log.info("player {} create agent: {}, UUID: {}", playerName, agent.getName(), agent.getUUID());
        return 1;
    }
}
