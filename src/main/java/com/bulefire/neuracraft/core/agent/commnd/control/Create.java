package com.bulefire.neuracraft.core.agent.commnd.control;

import com.bulefire.neuracraft.compatibility.command.FullCommand;
import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.PlayerJoinEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.Agent;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.util.NoAgentFound;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.extern.log4j.Log4j2;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.UUID;

@Log4j2
public class Create extends FullCommand.AbsCommand {
    @Override
    public int run(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        String agentName = StringArgumentType.getString(commandContext, "roomName");
        String sModel = StringArgumentType.getString(commandContext, "chatModel");
        String playerName = Objects.requireNonNull(commandContext.getSource().getPlayer()).getName().getString();
        UUID playerUUID = commandContext.getSource().getPlayer().getUUID();
        var agentManger = AgentController.getAgentManager();
        var playerManager = AgentController.getPlayerManager();

        Agent agent;
        try {
            agent = agentManger.creatAgent(sModel);
        } catch (NoAgentFound e) {
            feedback(commandContext.getSource(), Component.translatable("neuracraft.command.create.failure.unknownModel", sModel, agentManger.getAllAliveAgentKeys()));
            return 1;
        }

        agent.setName(agentName);
        APlayer player = new APlayer(playerName, playerUUID);
        agent.addPlayer(player);
        agent.addAdmin(player);
        playerManager.updatePlayer(player, agent.getUUID());
        agent.saveToFile(FileUtil.agent_base_url.resolve(agent.getModelName()).resolve(agent.getUUID().toString()));
        feedback(commandContext.getSource(), Component.translatable("neuracraft.command.create.success", agent.getName(), agent.getUUID()));
        var server = CUtil.getServer.get();
        ChatEventProcesser.ChatMessage.Env env = CUtil.getEnv(server);
        PlayerJoinEventProcesser.onPlayerJoin(new PlayerJoinEventProcesser.JoinMessage(player, env));
        log.info("player {} create agent: {}, UUID: {}", playerName, agent.getName(), agent.getUUID());
        return 1;
    }
}
