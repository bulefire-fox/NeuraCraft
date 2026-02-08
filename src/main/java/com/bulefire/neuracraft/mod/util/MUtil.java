package com.bulefire.neuracraft.mod.util;

import com.bulefire.neuracraft.compatibility.util.CUtil;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;

@Log4j2
public class MUtil {
    static {
        CUtil.hasMod = (modid) -> ModList.get().isLoaded(modid);
        log.debug("MUtil init");
        CUtil.getServer = ServerLifecycleHooks::getCurrentServer;
    }

    public static void init() {
    }
    
    public static void afterInit() {
    }
}
