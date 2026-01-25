package com.bulefire.neuracraft.client;

import com.bulefire.neuracraft.client.mod.event.listener.InitClientListener;
import net.fabricmc.api.ClientModInitializer;

public class NeuracraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        InitClientListener.init();
    }
}
