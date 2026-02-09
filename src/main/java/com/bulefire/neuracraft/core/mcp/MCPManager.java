package com.bulefire.neuracraft.core.mcp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MCPManager {
    // display name
    private final ConcurrentMap<String, MCPTool> tools;
    // name
    private final ConcurrentMap<String, MCPTool> toolsPath;
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public MCPManager() {
        tools = new ConcurrentHashMap<>();
        toolsPath = new ConcurrentHashMap<>();
    }
    
    public void registerTool(@NotNull MCPTool tool) {
        lock.writeLock().lock();
        try {
            if (tool.isValid()) throw new IllegalArgumentException("Invalid tool");
            if (tools.containsKey(tool.getDisplayName())) throw new IllegalArgumentException("Tool already exists");
            tools.put(tool.getDisplayName(), tool);
            toolsPath.put(tool.getInfo().getName(), tool);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void updateTool(@NotNull MCPTool tool) {
        lock.writeLock().lock();
        try {
            if (tool.isValid()) throw new IllegalArgumentException("Invalid tool");
            tools.put(tool.getDisplayName(), tool);
            toolsPath.put(tool.getInfo().getName(), tool);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public MCPTool removeToolByName(@NotNull String name) {
        lock.writeLock().lock();
        try {
            MCPTool tool = toolsPath.remove(name);
            if (tool == null) throw new IllegalArgumentException("Tool not found");
            tools.remove(tool.getDisplayName());
            return tool;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public MCPTool removeToolByDisplayName(@NotNull String displayName) {
        lock.writeLock().lock();
        try {
            MCPTool tool = tools.remove(displayName);
            if (tool == null) throw new IllegalArgumentException("Tool not found");
            tools.remove(tool.getInfo().getName());
            return tool;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public @Nullable MCPTool getToolByDisplayName(@NotNull String displayName) {
        return tools.get(displayName);
    }
    
    public @Nullable MCPTool getToolByName(@NotNull String name) {
        return toolsPath.get(name);
    }
    
    public @NotNull Collection<MCPTool> getTools() {
        return Set.copyOf(tools.values());
    }
}
