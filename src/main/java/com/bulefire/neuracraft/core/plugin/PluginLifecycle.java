package com.bulefire.neuracraft.core.plugin;

public interface PluginLifecycle {
    void initialize(PluginContext context);
    void shutdown();
}
