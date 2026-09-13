package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Log4j2
public class ClientChatEventListener {
    @SubscribeEvent
    public static void onChat(@NotNull ClientChatEvent event) {
        log.info("client catch player send chat");
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            throw new IllegalStateException("Minecraft.getInstance().player is null");
        }
        // In singleplayer the integrated server also emits ServerChatEvent.
        // Let the server-side event be the single source of truth.
        if (minecraft.isSingleplayer()) {
            return;
        }
        UUID uuid = minecraft.player.getUUID();
        String name = minecraft.player.getName().getString();
        ChatEventProcesser.onChat(
                new ChatEventProcesser.ChatMessage(
                        event.getMessage(),
                        new APlayer(name, uuid),
                        minecraft.isSingleplayer()
                                ? ChatEventProcesser.ChatMessage.Env.SINGLE
                                : ChatEventProcesser.ChatMessage.Env.CLIENT
                )
        );
    }
}
