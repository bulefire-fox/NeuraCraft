package com.bulefire.neuracraft;

import com.bulefire.neuracraft.mod.config.InitM;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

@Log4j2
public class Neuracraft implements ModInitializer {

    public static final String MOD_ID = "assets/neuracraft";

    public static final Path configPath = FabricLoader.getInstance().getConfigDir();
    public static final Path modsPath = FabricLoader.getInstance().getGameDir().resolve("mods");

    @Override
    public void onInitialize() {
        log.info("start init NC");
        // 初始化必须严格遵循NC->M->C->Core顺序
        // 初始化操作可以注入任意合理的部分
        InitM.init();
    }
}
