package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.function.process.ModLoadEventProcesser;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(modid = NeuraCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModLoadEventListener {
    @SubscribeEvent
    public static void loadComplete(FMLLoadCompleteEvent event) {
        ModLoadEventProcesser.onLoadComplete();
    }
}
