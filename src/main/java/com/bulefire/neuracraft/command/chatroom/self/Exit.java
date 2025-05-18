package com.bulefire.neuracraft.command.chatroom.self;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.ai.control.ChatRoomManger;
import com.bulefire.neuracraft.ai.control.NameManger;
import com.bulefire.neuracraft.ai.control.player.PlayerControl;
import com.bulefire.neuracraft.ai.control.player.PlayerMetaInfo;
import com.bulefire.neuracraft.command.chatroom.SubCommandBase;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Exit extends SubCommandBase {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String chatRoomName = StringArgumentType.getString(context, "roomName");
        String playerName = Objects.requireNonNull(context.getSource().getPlayer()).getName().getString();

        MutableComponent result = exit(playerName, chatRoomName, AIControl.getCm());
        feedback(context.getSource(), result);

        return SINGLE_SUCCESS;
    }

    public static @NotNull MutableComponent exit(@NotNull String name, @NotNull String chatRoomName, @NotNull ChatRoomManger cm){
        String cname = NameManger.getChatName(name);
        if (!cname.equals(chatRoomName)){
            return Component.translatable("neuracraft.command.exit.notInChatRoom");
        }
        AIChatRoom c = cm.getClient(cname);
        c.playerList.remove(name);
        PlayerMetaInfo m = PlayerControl.get(name);
        if (m != null) {
            m.setChatName(null);
        }
        return Component.translatable("neuracraft.command.exit.success",cname);
    }
}
