package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.PlayerExitEventProcesser;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerExitEventListener {
    @SubscribeEvent
    public static void onPlayerExit(PlayerEvent.@NotNull PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        String name = player.getName().getString();
        UUID uuid = player.getUUID();
        var server = ServerLifecycleHooks.getCurrentServer();
        ChatEventProcesser.ChatMessage.Env env;
        if (server == null) {
            env = ChatEventProcesser.ChatMessage.Env.CLIENT;
        } else if (server.isDedicatedServer()) {
            env = ChatEventProcesser.ChatMessage.Env.SERVER;
        } else {
            env = ChatEventProcesser.ChatMessage.Env.SINGLE;
        }
        PlayerExitEventProcesser.onPlayerExit(new PlayerExitEventProcesser.ExitMessage(new APlayer(name, uuid), env));
    }
}
