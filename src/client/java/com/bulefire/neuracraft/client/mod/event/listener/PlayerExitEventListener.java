package com.bulefire.neuracraft.client.mod.event.listener;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.function.process.PlayerExitEventProcesser;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class PlayerExitEventListener {

    static {
        ClientPlayConnectionEvents.DISCONNECT.register(
                PlayerExitEventListener::onPlayerExit
        );
    }

    public static void onPlayerExit(@NotNull ClientPacketListener event, Minecraft server) {
        Player player = Objects.requireNonNull(Minecraft.getInstance().player);
        String name = player.getName().getString();
        UUID uuid = player.getUUID();
        PlayerExitEventProcesser.onPlayerExit(
                new PlayerExitEventProcesser.ExitMessage(
                        new APlayer(name, uuid),
                        CUtil.getEnv(CUtil.getServer.get())
                )
        );
    }

    public static void init() {}
}
