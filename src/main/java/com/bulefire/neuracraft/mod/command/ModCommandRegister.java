package com.bulefire.neuracraft.mod.command;

import com.bulefire.neuracraft.compatibility.command.CommandRegister;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModCommandRegister {
    public static List<LiteralArgumentBuilder<CommandSourceStack>> literalArgumentBuilder = null;

    static {
        CommandRegistrationCallback.EVENT.register(ModCommandRegister::registerCommands);
    }

    public static void registerCommands(
            @NotNull CommandDispatcher<CommandSourceStack> dispatcher,
            @NotNull CommandBuildContext context,
            @NotNull Commands.CommandSelection environment
    ) {
        literalArgumentBuilder = CommandRegister.getLiteralArgumentBuilders();
        for (LiteralArgumentBuilder<CommandSourceStack> builder : literalArgumentBuilder) {
            dispatcher.register(builder);
        }
    }

    public static void init() {}
}
