package com.bulefire.neuracraft.core.plugin;

import java.nio.file.Path;

public record PluginContext(String pluginId, Path pluginPath, String apiVersion) {
}
