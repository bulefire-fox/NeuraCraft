package com.bulefire.neuracraft.mod.config;

import lombok.extern.log4j.Log4j2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Log4j2
public class BaseConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    @SubscribeEvent
    public static void onLoad(final @NotNull ModConfigEvent event) throws IOException {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            // 加载mod配置文件
            log.info("load config file {}", event.getConfig().getFileName());
        }
    }

    static {
        COMMON_SPEC = BUILDER.build();
    }
}
