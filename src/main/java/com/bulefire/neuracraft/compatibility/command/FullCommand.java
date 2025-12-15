package com.bulefire.neuracraft.compatibility.command;

import com.bulefire.neuracraft.compatibility.util.EmptyExecuteCommand;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record FullCommand(String literal, int permissionLevel, CommandArgument argument, AbsCommand executeCommand, List<FullCommand> subCommands, boolean isSubCommand) {
    public static final int USE_FATHER_PERMISSION_LEVEL = -168535486;

    public static final CommandArgument EMPTY_ARGUMENT = new CommandArgument(new ArrayList<>());
    public static final CommandArgument ARGUMENT_IS_SUBCOMMAND = new CommandArgument(new ArrayList<>());

    public static final AbsCommand EMPTY_EXECUTE_COMMAND = new AbsCommand() {
        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            throw new EmptyExecuteCommand("Empty execute command");
        }
    };

    public static final FullCommand EMPTY_SUBCOMMAND = new FullCommand(null, -1, EMPTY_ARGUMENT, EMPTY_EXECUTE_COMMAND, null, false);

    public FullCommand(String literal, int permissionLevel, CommandArgument argument, CommandRunnable<CommandContext<CommandSourceStack>, Integer> executeCommand, List<FullCommand> subCommands, boolean isSubCommand) {
        this(
                literal,
                permissionLevel,
                argument,
                new AbsCommand() {
                    @Override
                    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
                        return executeCommand.run(context);
                    }
                },
                subCommands,
                isSubCommand
        );
    }

    public abstract static class AbsCommand implements Command<CommandSourceStack> {
        protected static final Component PREFIX = Component.literal("§7[§bNeuraCraft§7]§r ");

        protected void feedback(@NotNull CommandSourceStack source, @NotNull String message){
            source.sendSuccess(() -> PREFIX.copy().append(message), false);
        }

        protected void feedback(@NotNull CommandSourceStack source, @NotNull Component message){
            source.sendSuccess(() -> PREFIX.copy().append(message), false);
        }
    }

    public record CommandArgument(List<Argument> arguments) {
        public CommandArgument() {
            this(new ArrayList<>());
        }

        public CommandArgument addArgument(StringArgumentType type, String description) {
            this.arguments.add(new Argument(type, description));
            return this;
        }

        public CommandArgument addArgument(Argument argument) {
            this.arguments.add(argument);
            return this;
        }

        public record Argument(StringArgumentType type, String description) {
        }
    }

    public interface CommandRunnable<T, R> {
        R run(T context) throws CommandSyntaxException;
    }
}
