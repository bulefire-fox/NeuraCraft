package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.compatibility.function.process.ModLoadEventProcesser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

@Environment(EnvType.SERVER)
public class ModLoadEventListener {

    static {
        ServerLifecycleEvents.SERVER_STARTED.register(ModLoadEventListener::loadComplete);
    }

    public static void loadComplete(MinecraftServer event) {
        ModLoadEventProcesser.onLoadComplete();
    }

    public static void init() {}
}
