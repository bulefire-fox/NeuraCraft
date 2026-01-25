package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Environment(EnvType.SERVER)
public class ChatEventListener {

    static {
        ServerMessageEvents.CHAT_MESSAGE.register(
                ChatEventListener::onServerChat
        );
    }

    @Environment(EnvType.SERVER)
    public static void onServerChat(@NotNull PlayerChatMessage event, Player player, ChatType.Bound bound) {
        log.info("server catch player send chat");
        // 获取消息文本
        String message;
        if (event.unsignedContent() != null) {
            message = event.unsignedContent().getString();
        } else {
            message = event.signedContent();
        }
        // 获取玩家名称
        String name;
        UUID uuid = player.getUUID();
        name = player.getName().getString();
        CompletableFuture.runAsync(() -> ChatEventProcesser.onChat(
                new ChatEventProcesser.ChatMessage(
                        message,
                        new APlayer(name, uuid),
                        CUtil.getEnv(CUtil.getServer.get())
                )
        ));
    }

    public static void init() {}
}
