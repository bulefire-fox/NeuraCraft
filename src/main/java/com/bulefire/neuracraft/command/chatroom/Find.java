package com.bulefire.neuracraft.command.chatroom;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.ai.control.ChatRoomManger;
import com.bulefire.neuracraft.ai.control.NameManger;
import com.bulefire.neuracraft.ai.control.NoChatRoomFound;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Find extends SubCommandBase{
    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = Objects.requireNonNull(context.getSource().getPlayer()).getName().getString();
        ChatRoomManger cm = AIControl.getCm();

        MutableComponent result = find(name, cm);
        feedback(context.getSource(), result);
        return SINGLE_SUCCESS;
    }

    private static @NotNull MutableComponent find(@NotNull String name, @NotNull ChatRoomManger cm){
        try {
            AIChatRoom c = cm.getClient(NameManger.getChatName(name));
            return Component.translatable("neuracraft.command.find.success", c.getName());
        } catch (NoChatRoomFound e) {
            return Component.translatable("neuracraft.command.find.notInChatRoom");
        }
    }
}
