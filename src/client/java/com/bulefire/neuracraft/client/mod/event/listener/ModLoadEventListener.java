package com.bulefire.neuracraft.client.mod.event.listener;

import com.bulefire.neuracraft.compatibility.function.process.ModLoadEventProcesser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class ModLoadEventListener {

    static {
        ClientLifecycleEvents.CLIENT_STOPPING.register(ModLoadEventListener::loadComplete);
    }

    public static void loadComplete(Minecraft event) {
        ModLoadEventProcesser.onLoadComplete();
    }

    public static void init() {}
}
