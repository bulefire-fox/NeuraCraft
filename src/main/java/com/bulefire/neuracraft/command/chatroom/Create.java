package com.bulefire.neuracraft.command.chatroom;

import com.bulefire.neuracraft.ai.AIModels;
import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.ai.control.ChatRoomManger;
import com.bulefire.neuracraft.ai.control.NoChatRoomFound;
import com.bulefire.neuracraft.ai.control.player.PlayerControl;
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

public class Create extends SubCommandBase{
    private static final Logger logger = LogUtils.getLogger();

    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // 构建参数
        String chatRoomName = StringArgumentType.getString(context, "roomName");
        String playerName = Objects.requireNonNull(context.getSource().getPlayer()).getName().getString();
        // 执行命令
        MutableComponent result = create(playerName, chatRoomName, AIControl.getCm());
        feedback(context.getSource(), result);
        // 1
        return SINGLE_SUCCESS;
    }

    public static @NotNull MutableComponent create(@NotNull String name, @NotNull String cname, @NotNull ChatRoomManger cm){
        logger.info("start create room");
        if(!cm.createClient(cname, AIModels.CyberFurry)){
            return Component.translatable("neuracraft.command.create.failure.alreadyExist");
        }

        try {
            cm.getClient(cname).playerList.add(name);
        } catch (NoChatRoomFound e) {
            logger.error(" NoChatRoomFound: {}", e.getMessage());
            return Component.translatable("neuracraft.command.create.failure.unknown");
        }


        if (PlayerControl.get(name) == null){
            return Component.translatable("neuracraft.command.create.failure.unknown");
        }

        Objects.requireNonNull(PlayerControl.get(name)).setChatName(cname);
        try {
            PlayerControl.saveAllPlayerToFile(List.of(name));
        }catch (IOException e){
            logger.error("save player IOException: {}", e.getMessage());
        }
        logger.info("control create {}",cname);
        return Component.translatable("neuracraft.command.create.success", cname);
    }

}
