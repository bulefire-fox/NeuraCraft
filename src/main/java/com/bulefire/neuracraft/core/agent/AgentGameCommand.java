package com.bulefire.neuracraft.core.agent;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AgentGameCommand {

    public List<LiteralArgumentBuilder<CommandSourceStack>> commands;

    public AgentGameCommand() {
        commands = new ArrayList<>();
    }

    public void registerCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> command) {
        commands.add(command);
    }

    public void deleteCommand(@NotNull LiteralArgumentBuilder<CommandSourceStack> command) {
        commands.remove(command);
    }

    public List<LiteralArgumentBuilder<CommandSourceStack>> getAllCommands(){
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
