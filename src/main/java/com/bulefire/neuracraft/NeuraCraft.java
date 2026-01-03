package com.bulefire.neuracraft;

import com.bulefire.neuracraft.mod.config.InitM;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod(NeuraCraft.MOD_ID)
@Log4j2
public class NeuraCraft {
    public static final String MOD_ID = "neuracraft";

    public static final Path configPath = FMLPaths.CONFIGDIR.get();
    public static final Path modsPath = FMLPaths.MODSDIR.get();;

    public NeuraCraft() {
        log.info("start init NC");
        // 初始化必须严格遵循NC->M->C->Core顺序
        // 初始化操作可以注入任意合理的部分
        InitM.init();
    }
}
