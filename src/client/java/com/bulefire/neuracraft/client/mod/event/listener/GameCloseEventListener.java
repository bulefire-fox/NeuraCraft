package com.bulefire.neuracraft.client.mod.event.listener;

import com.bulefire.neuracraft.compatibility.function.process.ServerStoppingEventProcesser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class GameCloseEventListener {

    static {
        ClientLifecycleEvents.CLIENT_STOPPING.register(GameCloseEventListener::onGameClose);
    }

    public static void onGameClose(Minecraft event) {
        ServerStoppingEventProcesser.onServerStopping();
    }

    public static void init() {}
}
