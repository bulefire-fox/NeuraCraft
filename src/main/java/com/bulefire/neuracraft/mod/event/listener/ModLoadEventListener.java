package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.compatibility.function.process.ModLoadEventProcesser;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModLoadEventListener {
    @SubscribeEvent
    public static void loadComplete(FMLCommonSetupEvent event) {
        ModLoadEventProcesser.onLoadComplete();
    }
}
