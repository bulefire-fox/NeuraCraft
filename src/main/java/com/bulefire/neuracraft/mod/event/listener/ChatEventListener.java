package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import lombok.extern.log4j.Log4j2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Log4j2
public class ChatEventListener {
    @SubscribeEvent
    public static void onServerChat(@NotNull ServerChatEvent event) {
        log.info("server catch player send chat");
        String message = event.getMessage().getString();
        UUID uuid = event.getPlayer().getUUID();
        String name = event.getPlayer().getName().getString();
        ChatEventProcesser.onChat(
                new ChatEventProcesser.ChatMessage(
                        message,
                        new APlayer(name, uuid),
                        ChatEventProcesser.ChatMessage.Env.SERVER
                )
        );
    }
}
