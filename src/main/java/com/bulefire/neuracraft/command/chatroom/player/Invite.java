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

public class Invite extends SubCommandBase {
    private static final Logger logger = LogUtils.getLogger();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // 参数解析
        String inviteName = StringArgumentType.getString(context, "inviteName");
        String playerName = Objects.requireNonNull(context.getSource().getPlayer()).getName().getString();
        //处理
        MutableComponent result = invite(playerName, inviteName, AIControl.getCm());
        // 回调
        feedback(context.getSource(), result);
        // 1
        return SINGLE_SUCCESS;
    }

    private static @NotNull MutableComponent invite(@NotNull String playerName, @NotNull String inviteName, @NotNull ChatRoomManger cm){
        PlayerMetaInfo ppm = PlayerControl.get(playerName);
        PlayerMetaInfo ipm = PlayerControl.get(inviteName);
        // 玩家不存在
        if (ppm == null || ipm == null){
            return Component.translatable("neuracraft.command.invite.notFound",inviteName);
        }

        String cname = ppm.getChatName();
        AIChatRoom c = cm.getClient(cname);

        if (!c.adminList.contains(playerName)) {
            return Component.translatable("neuracraft.command.invite.notAdmin",cname);
        }

        c.playerList.add(inviteName);
        ipm.setChatName(cname);

        try {
            PlayerControl.saveAllPlayerToFile(List.of(inviteName));
            c.save();
        }catch (Exception e){
            logger.error("save player IOException: {}", e.getMessage());
        }
        return Component.translatable("neuracraft.command.invite.success", inviteName,cname);
    }
}
