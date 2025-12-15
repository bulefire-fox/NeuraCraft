package com.bulefire.neuracraft.mod.config;

import com.bulefire.neuracraft.NeuraCraft;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = NeuraCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Log4j2
public class BaseConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec COMMON_SPEC;

    @SubscribeEvent
    public static void onLoad(final @NotNull ModConfigEvent event) throws IOException {
        if (event.getConfig().getSpec() == COMMON_SPEC){
            // 加载mod配置文件
            log.info("load config file {}", event.getConfig().getFileName());
        }
    }

    static {
        COMMON_SPEC = BUILDER.build();
        MinecraftForge.EVENT_BUS.register(BaseConfig.class);
    }
}
