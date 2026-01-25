package com.bulefire.neuracraft.client.mod.event.listener;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Environment(EnvType.CLIENT)
public class ClientChatEventListener {

    static {
        ClientSendMessageEvents.CHAT.register(
                ClientChatEventListener::onClientChat
        );
    }

    @Environment(EnvType.CLIENT)
    public static void onClientChat(@NotNull String message) {
        log.info("client catch player send chat");
        String name;
        Player player = Objects.requireNonNull(Minecraft.getInstance().player);
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
