package com.bulefire.neuracraft.config;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.config.yy.BaseInformation;
import com.bulefire.neuracraft.config.yy.Variables;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

// 一个示例 config 类。这不是必需的，但最好有一个来保持您的配置井井有条。
// 演示如何使用 Forge 的配置 API
@Mod.EventBusSubscriber(modid = NeuraCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final Logger log = LogUtils.getLogger();

    // 构建器 Builder 用于构建配置
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    // 构建器构建配置
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final BaseInformation BASE_INFORMATION;

    @SubscribeEvent
    static void onLoad(final @NotNull ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC){
            log.info("Loaded config file {}", event.getConfig().getFileName());
            init();
        }
    }

    static {
        BASE_INFORMATION = new BaseInformation();

        COMMON_SPEC = BUILDER.build();
        MinecraftForge.EVENT_BUS.register(Config.class);
    }

    public static void init() {
        BASE_INFORMATION.init();
    }
}
