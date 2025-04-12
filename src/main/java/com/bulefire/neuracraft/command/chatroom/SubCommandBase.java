package com.bulefire.neuracraft.command.chatroom;

import com.mojang.brigadier.Command;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class SubCommandBase implements Command<CommandSourceStack> {
    protected static final Component PREFIX = Component.literal("§7[§bNeuraCraft§7]§r ");

    protected void feedback(@NotNull CommandSourceStack source, @NotNull String message){
        source.sendSuccess(() -> PREFIX.copy().append(message), false);
    }

    protected void feedback(@NotNull CommandSourceStack source, @NotNull Component message){
        source.sendSuccess(() -> PREFIX.copy().append(message), false);
    }
}
