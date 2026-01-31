package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = NeuraCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerStartedEventListener {
    @SubscribeEvent
    public static void onServerStarted(@NotNull ServerStartedEvent event) {
        NeuraCraft.afterInit();
    }
    
    @SubscribeEvent
    public static void onServerStarted(@NotNull FMLClientSetupEvent event) {
        NeuraCraft.afterInit();
    }
}
