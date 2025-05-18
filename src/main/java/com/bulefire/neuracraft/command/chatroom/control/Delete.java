package com.bulefire.neuracraft.command.chatroom.control;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.ai.control.ChatRoomManger;
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

import java.io.IOException;
import java.util.Objects;

public class Delete extends SubCommandBase {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String chatRoomName = StringArgumentType.getString(context, "roomName");
        String playerName = Objects.requireNonNull(context.getSource().getPlayer()).getName().getString();

        MutableComponent result = delete(playerName, chatRoomName, AIControl.getCm());
        feedback(context.getSource(), result);
        return 1;
    }

    public static @NotNull MutableComponent delete(@NotNull String name, String cname, @NotNull ChatRoomManger cm){
        AIChatRoom c = cm.getClient(cname);
        PlayerMetaInfo pm = PlayerControl.get(name);
        if (pm != null){
            if (!c.adminList.contains(name)){
                return Component.translatable("neuracraft.command.delete.notAdmin",cname);
            }

            if (Objects.equals(pm.getChatName(), cname)){
                pm.setChatName(null);
            }
        }
        try {
            c.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cm.removeClient(cname);
        return Component.translatable("neuracraft.command.delete.success",cname);
    }
}