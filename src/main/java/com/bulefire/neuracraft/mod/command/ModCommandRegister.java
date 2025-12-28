package com.bulefire.neuracraft.mod.command;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.command.CommandRegister;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Mod.EventBusSubscriber(modid = NeuraCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommandRegister {
    public static List<LiteralArgumentBuilder<CommandSourceStack>> literalArgumentBuilder = null;

    @SubscribeEvent
    public static void registerCommands(@NotNull RegisterCommandsEvent event) {
        if (CommandRegister.getLiteralArgumentBuilders() == null)
            return;
        else literalArgumentBuilder = CommandRegister.getLiteralArgumentBuilders();
        for (LiteralArgumentBuilder<CommandSourceStack> builder : literalArgumentBuilder) {
            event.getDispatcher().register(builder);
        }
    }
}
