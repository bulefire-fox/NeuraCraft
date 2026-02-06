package com.bulefire.neuracraft.core.mcp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MCPManager {
    private final ConcurrentMap<String, MCPTool> tools;
    private final ConcurrentMap<String, MCPTool> toolsPath;
    
    public MCPManager() {
        tools = new ConcurrentHashMap<>();
        toolsPath = new ConcurrentHashMap<>();
    }
    
    public void registerTool(@NotNull MCPTool tool) {
        if (tool.isValid()) throw new IllegalArgumentException("Invalid tool");
        if (tools.containsKey(tool.getName())) throw new IllegalArgumentException("Tool already exists");
        tools.put(tool.getName(), tool);
        toolsPath.put(tool.getInfo().getMethod(), tool);
    }
    
    public void updateTool(@NotNull MCPTool tool) {
        if (tool.isValid()) throw new IllegalArgumentException("Invalid tool");
        tools.put(tool.getName(), tool);
        toolsPath.put(tool.getInfo().getMethod(), tool);
    }
    
    public @Nullable MCPTool getToolByName(@NotNull String name) {
        return tools.get(name);
    }
    
    public @Nullable MCPTool getToolByMethod(@NotNull String name) {
        return toolsPath.get(name);
    }
    
    public @NotNull Collection<MCPTool> getTools() {
        return tools.values();
    }
}
