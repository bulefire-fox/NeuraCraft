package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.function.process.LevelLoadEventProcess;
import com.bulefire.neuracraft.compatibility.function.process.LevelUnloadEventProcess;
import com.bulefire.neuracraft.mod.config.InitM;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = NeuraCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelLoadEventListener {
    @SubscribeEvent
    public static void onLevelLoad(@NotNull LevelEvent.Load event) {
        LevelLoadEventProcess.onLeveLoad();
    }
    
    @SubscribeEvent
    public static void onLevelUnload(@NotNull LevelEvent.Unload event) {
        LevelUnloadEventProcess.onLeveUnload();
    }
}
