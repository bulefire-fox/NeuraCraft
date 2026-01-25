package com.bulefire.neuracraft.client.mod.event.listener;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.PlayerJoinEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@Log4j2
@Environment(EnvType.CLIENT)
public class PlayerJoinEventListener {

    static {
        ClientPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> onPlayerJoin(handler)
        );
    }

    public static void onPlayerJoin(@NotNull ClientPacketListener event) {
        log.debug("Catch Player Join");
        Player player = Objects.requireNonNull(Minecraft.getInstance().player);
        String name = player.getName().getString();
        UUID uuid = player.getUUID();
        var server = CUtil.getServer.get();
        ChatEventProcesser.ChatMessage.Env env = CUtil.getEnv(server);
        CUtil.getPlayer = () -> player;
        PlayerJoinEventProcesser.onPlayerJoin(new PlayerJoinEventProcesser.JoinMessage(new APlayer(name, uuid), env));
    }

    public static void init() {}
}
