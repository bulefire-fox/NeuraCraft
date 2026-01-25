package com.bulefire.neuracraft.mod.event.listener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.SERVER)
public class InitServerListener {
    public static void init() {
        ChatEventListener.init();
        GameCloseEventListener.init();
        ModLoadEventListener.init();
        PlayerExitEventListener.init();
        PlayerJoinEventListener.init();
    }
}
