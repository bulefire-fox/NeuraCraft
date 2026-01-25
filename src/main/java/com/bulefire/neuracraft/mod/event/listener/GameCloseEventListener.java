package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.compatibility.function.process.ServerStoppingEventProcesser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

@Environment(EnvType.SERVER)
public class GameCloseEventListener {

    static {
        ServerLifecycleEvents.SERVER_STOPPING.register(GameCloseEventListener::onGameClose);
    }

    public static void onGameClose(MinecraftServer event) {
        ServerStoppingEventProcesser.onServerStopping();
    }

    public static void init() {}
}
