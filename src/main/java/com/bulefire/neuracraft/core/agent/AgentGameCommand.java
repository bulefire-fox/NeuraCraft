package com.bulefire.neuracraft.core.agent;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 命令管理
 *
 * @author bulefire_fox
 * @version 1.0
 * @see AgentController
 * @since 2.0
 */
public class AgentGameCommand {
    public static final LiteralArgumentBuilder<CommandSourceStack> baseCommand = Commands.literal("agent");
    public static final LiteralArgumentBuilder<CommandSourceStack> pluginBaseCommand = Commands.literal("plugin");

    private final List<LiteralArgumentBuilder<CommandSourceStack>> commands;

    public AgentGameCommand() {
        commands = new ArrayList<>();
    }

    public void registerCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> command) {
        commands.add(command);
    }

    public void deleteCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> command) {
        commands.remove(command);
    }

    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommand() {
        return baseCommand;
    }

    public LiteralArgumentBuilder<CommandSourceStack> getPluginBaseCommand() {
        return pluginBaseCommand;
    }

    public List<LiteralArgumentBuilder<CommandSourceStack>> getAllCommands() {
        baseCommand.then(pluginBaseCommand);
        commands.add(baseCommand);
        return new ArrayList<>(commands);
    }

    //    public List<FullCommand> commands;
    //
    //    public AgentGameCommand() {
    //        commands = new ArrayList<>();
    //    }
    //
    //    public AgentGameCommand(@NotNull List<FullCommand> commands) {
    //        this.commands = commands;
    //    }
    //
    //    public void registerCommand(@NotNull FullCommand command) {
    //        commands.add(command);
    //    }
    //
    //    public void deleteCommand(@NotNull FullCommand command) {
    //        this.commands.remove(command);
    //    }
    //
    //    public List<FullCommand> getAllCommands(){
    //        return new ArrayList<>(commands);
    //    }
}
