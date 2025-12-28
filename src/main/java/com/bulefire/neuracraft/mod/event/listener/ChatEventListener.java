package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Mod.EventBusSubscriber(modid = NeuraCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatEventListener {
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onChat(@NotNull ClientChatEvent event) {
        log.info("client catch player send chat");
        // 获取消息文本
        String message = event.getMessage();
        // 获取玩家名称
        String name;
        UUID uuid;
        if (Minecraft.getInstance().player != null) {
            name = Minecraft.getInstance().player.getName().getString();
            uuid = Minecraft.getInstance().player.getUUID();
        } else {
            throw new RuntimeException("Minecraft.getInstance().player is null");
        }

        // 异步
        CompletableFuture.runAsync(() -> ChatEventProcesser.onChat(
                new ChatEventProcesser.ChatMessage(
                        message,
                        new APlayer(name, uuid),
                        Minecraft.getInstance().isSingleplayer() ? ChatEventProcesser.ChatMessage.Env.SINGLE : ChatEventProcesser.ChatMessage.Env.CLIENT)
        ));
    }

    @SubscribeEvent
    @OnlyIn(Dist.DEDICATED_SERVER)
    public static void onServerChat(@NotNull ServerChatEvent event) {
        log.info("server catch player send chat");
        // 获取消息文本
        String message = event.getMessage().getString();
        // 获取玩家名称
        String name;
        UUID uuid = event.getPlayer().getUUID();
        if (event.getPlayer() != null) {
            name = event.getPlayer().getName().getString();
        } else {
            throw new RuntimeException("Minecraft.getInstance().player is null");
        }
        CompletableFuture.runAsync(() -> ChatEventProcesser.onChat(
                new ChatEventProcesser.ChatMessage(
                        message,
                        new APlayer(name, uuid),
                        ChatEventProcesser.ChatMessage.Env.SERVER)
        ));
    }
}
