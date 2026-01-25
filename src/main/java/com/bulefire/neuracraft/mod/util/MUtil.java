package com.bulefire.neuracraft.mod.util;

import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.mod.command.ModCommandRegister;
import com.bulefire.neuracraft.mod.event.listener.InitServerListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

public class MUtil {

    private static MinecraftServer currentServer = null;

    static {
        CUtil.hasMod = (modid) -> FabricLoader.getInstance().isModLoaded(modid);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> currentServer = null);

        CUtil.getServer = () -> currentServer;
    }

    public static void init() {
        EnvType env = FabricLoader.getInstance().getEnvironmentType();
        if (env == EnvType.SERVER)
            InitServerListener.init();
        ModCommandRegister.init();
    }
}
