package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class ServerStartedEventListener {
    @SubscribeEvent
    public static void onServerStarted(@NotNull ServerStartedEvent event) {
        NeuraCraft.afterInit();
    }
}
