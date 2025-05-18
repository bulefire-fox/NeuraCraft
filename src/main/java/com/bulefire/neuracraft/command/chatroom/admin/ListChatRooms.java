package com.bulefire.neuracraft.command.chatroom.admin;

import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.command.chatroom.SubCommandBase;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListChatRooms extends SubCommandBase {
    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<String> cs = AIControl.getCm().getAllClients();
        feedback(context.getSource(), Component.translatable("neuracraft.command.list.chatroom", cs));
        return SINGLE_SUCCESS;
    }
}
