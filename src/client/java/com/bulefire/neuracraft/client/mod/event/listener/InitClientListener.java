package com.bulefire.neuracraft.client.mod.event.listener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class InitClientListener {
    public static void init() {
        ClientChatEventListener.init();
        GameCloseEventListener.init();
        ModLoadEventListener.init();
        PlayerExitEventListener.init();
        PlayerJoinEventListener.init();
    }
}
