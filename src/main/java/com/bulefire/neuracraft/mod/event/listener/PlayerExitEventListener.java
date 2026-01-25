package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import com.bulefire.neuracraft.compatibility.function.process.PlayerExitEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Environment(EnvType.SERVER)
public class PlayerExitEventListener {

    static {
        ServerPlayConnectionEvents.DISCONNECT.register(
                PlayerExitEventListener::onPlayerExit
        );
    }

    public static void onPlayerExit(@NotNull ServerGamePacketListenerImpl event, MinecraftServer server) {
        Player player = event.getPlayer();
        String name = player.getName().getString();
        UUID uuid = player.getUUID();
        ChatEventProcesser.ChatMessage.Env env = CUtil.getEnv(server);
        PlayerExitEventProcesser.onPlayerExit(new PlayerExitEventProcesser.ExitMessage(new APlayer(name, uuid), env));
    }

    public static void init() {}
}
