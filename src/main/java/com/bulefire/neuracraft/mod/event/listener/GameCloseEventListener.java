package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.function.process.ServerStoppingEventProcesser;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NeuraCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GameCloseEventListener {
    @SubscribeEvent
    public static void onGameClose(ServerStoppingEvent event) {
        ServerStoppingEventProcesser.onServerStopping();
    }
}
