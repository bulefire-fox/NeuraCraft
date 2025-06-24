package com.bulefire.neuracraft.command.chatroom.control;

import com.bulefire.neuracraft.ai.AIModels;
import com.bulefire.neuracraft.ai.control.AIControl;
import com.bulefire.neuracraft.ai.control.ChatRoomManger;
import com.bulefire.neuracraft.ai.control.NoChatRoomFound;
import com.bulefire.neuracraft.ai.control.player.PlayerControl;
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

public class Create extends SubCommandBase {
    private static final Logger logger = LogUtils.getLogger();

    @Override
    public int run(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // 构建参数
        String chatRoomName = StringArgumentType.getString(context, "roomName");
        AIModels model = AIModels.getModel(StringArgumentType.getString(context, "chatModel"));
        String playerName = Objects.requireNonNull(context.getSource().getPlayer()).getName().getString();
        // 执行命令
        MutableComponent result = create(playerName, chatRoomName, model, AIControl.getCm());
        feedback(context.getSource(), result);
        // 1
        return SINGLE_SUCCESS;
    }

    public static @NotNull MutableComponent create(@NotNull String name, @NotNull String cname, AIModels model, @NotNull ChatRoomManger cm){
        logger.info("start create room");
        if(!cm.createClient(cname, model)){
            return Component.translatable("neuracraft.command.create.failure.alreadyExist", cname);
        }

        try {
            cm.getClient(cname).playerList.add(name);
            cm.getClient(cname).adminList.add(name);
        } catch (NoChatRoomFound e) {
            logger.error("NoChatRoomFound: {}", e.getMessage());
            return Component.translatable("neuracraft.command.create.failure.unknown");
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage());
        }

        logger.debug("create room done");
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
