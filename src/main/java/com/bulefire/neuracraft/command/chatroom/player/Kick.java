package com.bulefire.neuracraft.command.chatroom.player;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.ai.control.ChatRoomManger;
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

import java.util.List;
import java.util.Objects;

public class Kick extends SubCommandBase {
    private static final Logger logger = LogUtils.getLogger();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // 参数解析
        String kickName = StringArgumentType.getString(context, "kickName");
        String playerName = Objects.requireNonNull(context.getSource().getPlayer()).getName().getString();
        //处理
        MutableComponent result = kick(playerName, kickName, AIControl.getCm());
        // 回调
        feedback(context.getSource(), result);
        // 1
        return SINGLE_SUCCESS;
    }

    public static @NotNull MutableComponent kick(@NotNull String playerName, @NotNull String kickName, @NotNull ChatRoomManger cm) {
        PlayerMetaInfo ppm = PlayerControl.get(playerName);
        PlayerMetaInfo kpm = PlayerControl.get(kickName);
        if (ppm == null || kpm == null){
            return Component.translatable("neuracraft.command.kick.notFound",kickName);
        }

        if (!ppm.getChatName().equals(kpm.getChatName())){
            return Component.translatable("neuracraft.command.kick.notInChatRoom",kickName,ppm.getChatName());
        }

        AIChatRoom c = cm.getClient(ppm.getChatName());
        c.adminList.remove(kickName);
        c.playerList.remove(kickName);
        kpm.setChatName(null);
        try {
            PlayerControl.saveAllPlayerToFile(List.of(kickName));
            c.save();
        }catch (Exception e){
            logger.error("save player IOException: {}", e.getMessage());
        }
        return Component.translatable("neuracraft.command.kick.success", ppm.getChatName(), kickName);
    }
}
