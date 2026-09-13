package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.PlayerJoinEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import lombok.extern.log4j.Log4j2;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Log4j2
public class PlayerJoinEventListener {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        log.debug("Catch Player Join");
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
        CUtil.getPlayer = () -> player;
        PlayerJoinEventProcesser.onPlayerJoin(new PlayerJoinEventProcesser.JoinMessage(new APlayer(name, uuid), env));
    }
}
