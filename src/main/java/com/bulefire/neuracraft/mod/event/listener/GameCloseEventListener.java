package com.bulefire.neuracraft.mod.event.listener;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.function.process.ServerStoppingEventProcesser;
import com.bulefire.neuracraft.core.config.ConfigFileWatcher;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.plugin.PluginLoader;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class GameCloseEventListener {
    @SubscribeEvent
    public static void onGameClose(ServerStoppingEvent event) {
        AgentController.getInstance().stop();
        ServerStoppingEventProcesser.onServerStopping();
        ConfigFileWatcher.stop();
        MCPController.getInstance().stopExternal();
        PluginLoader.getInstance().shutdown();
    }
}
