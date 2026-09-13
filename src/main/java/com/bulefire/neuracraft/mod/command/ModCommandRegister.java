package com.bulefire.neuracraft.mod.command;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.command.CommandRegister;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModCommandRegister {
    public static List<LiteralArgumentBuilder<CommandSourceStack>> literalArgumentBuilder = null;

    @SubscribeEvent
    public static void registerCommands(@NotNull RegisterCommandsEvent event) {
        if (CommandRegister.getLiteralArgumentBuilders() == null)
            return;
        else
            literalArgumentBuilder = CommandRegister.getLiteralArgumentBuilders();
        for (LiteralArgumentBuilder<CommandSourceStack> builder : literalArgumentBuilder) {
            event.getDispatcher().register(builder);
        }
    }
}
