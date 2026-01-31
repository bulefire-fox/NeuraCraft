package com.bulefire.neuracraft.mod.util;

import com.bulefire.neuracraft.compatibility.util.CUtil;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;

public class MUtil {
    static {
        CUtil.hasMod = (modid) -> ModList.get().isLoaded(modid);
        CUtil.getServer = ServerLifecycleHooks::getCurrentServer;
    }

    public static void init() {
    }
    
    public static void afterInit() {
    }
}
