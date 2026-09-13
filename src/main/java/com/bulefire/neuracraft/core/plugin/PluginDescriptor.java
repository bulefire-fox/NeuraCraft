package com.bulefire.neuracraft.core.plugin;

import com.google.gson.Gson;

import java.io.Reader;

public record PluginDescriptor(String id, String version, String apiVersion, String mainClass) {
    public static PluginDescriptor read(Reader reader) {
        return new Gson().fromJson(reader, PluginDescriptor.class);
    }
}
