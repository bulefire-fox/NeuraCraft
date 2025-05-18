package com.bulefire.neuracraft.command.chatroom.self;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.ai.control.ChatRoomManger;
import com.bulefire.neuracraft.ai.control.NameManger;
import com.bulefire.neuracraft.ai.control.NoChatRoomFound;
import com.bulefire.neuracraft.ai.control.player.PlayerControl;
import com.bulefire.neuracraft.ai.control.player.PlayerMetaInfo;
import com.bulefire.neuracraft.command.chatroom.SubCommandBase;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Join extends SubCommandBase {
    private static final Logger logger = LogUtils.getLogger();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // 参数解析
        String chatRoomName = StringArgumentType.getString(context, "roomName");
        String playerName = Objects.requireNonNull(context.getSource().getPlayer()).getName().getString();
        //处理
        MutableComponent result = join(playerName, chatRoomName, AIControl.getCm());
        // 回调
        feedback(context.getSource(), result);
        // 1
        return SINGLE_SUCCESS;
    }

    public static @NotNull MutableComponent join(@NotNull String name, @NotNull String chat, @NotNull ChatRoomManger cm){
        logger.info("start join room");

        AIChatRoom new_c;
        try {
            new_c = cm.getClient(chat);
        } catch (NoChatRoomFound e) {
            return Component.translatable("neuracraft.command.join.failure.notExist", chat);
        }

        AIChatRoom old_c;
        try {
            old_c = cm.getClient(NameManger.getChatName(name));
            old_c.playerList.remove(name);
            old_c.save();
        } catch (NoChatRoomFound e) {
            //return Component.translatable("neuracraft.command.join.failure.notExist", chat);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new_c.playerList.add(name);
        PlayerMetaInfo m = PlayerControl.get(name);
        if (m != null) {
            m.setChatName(chat);
        }
        try {
            new_c.save();
            PlayerControl.saveAllPlayerToFile(List.of(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Component.translatable("neuracraft.command.join.success", chat);
    }
}
