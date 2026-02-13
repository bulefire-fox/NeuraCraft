package com.bulefire.neuracraft.mod.util;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Objects;

@Log4j2
public class MUtil {
    static {
        CUtil.hasMod = (modid) -> ModList.get().isLoaded(modid);
        log.debug("MUtil init");
        CUtil.getServer = ServerLifecycleHooks::getCurrentServer;
        CUtil.getModJarPath = ()-> Objects.requireNonNull(ModList.get().getModFileById(NeuraCraft.MOD_ID).getFile().getScanResult()).getIModInfoData().get(0).getFile().getFilePath();
    }

    public static void init() {
    }
    
    public static void afterInit() {
    }
}
