package com.bulefire.neuracraft.core.mcp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Registry for all internal and external tools.
 *
 * <p>The tool ID map is the source of truth. Display names and server names are
 * only indexes, so replacing or removing one tool cannot affect its siblings.</p>
 */
public class MCPManager {
    private final ConcurrentMap<ToolId, MCPTool> tools = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<ToolId>> displayIndex = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void registerTool(@NotNull MCPTool tool) {
        if (!tool.isValid()) {
            throw new IllegalArgumentException("Invalid tool");
        }

        lock.writeLock().lock();
        try {
            ToolId id = tool.getId();
            MCPTool previous = tools.put(id, tool);
            if (previous != null) {
                removeFromIndex(displayIndex, previous.getDisplayName(), id);
            }
            displayIndex.computeIfAbsent(tool.getDisplayName(), ignored -> ConcurrentHashMap.newKeySet()).add(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Deprecated
    public void updateTool(@NotNull MCPTool tool) {
        registerTool(tool);
    }

    public @Nullable MCPTool getTool(@NotNull ToolId id) {
        return tools.get(id);
    }

    public @Nullable MCPTool getToolByName(@NotNull String name) {
        return getTool(new ToolId(name));
    }

    public @Nullable Set<MCPTool> getToolByDisplayName(@NotNull String displayName) {
        Set<ToolId> ids = displayIndex.get(displayName);
        if (ids == null) {
            return null;
        }
        Set<MCPTool> result = new LinkedHashSet<>();
        for (ToolId id : ids) {
            MCPTool tool = tools.get(id);
            if (tool != null) {
                result.add(tool);
            }
        }
        return Set.copyOf(result);
    }

    public @NotNull Collection<MCPTool> getTools() {
        return tools.values().stream()
                .sorted(Comparator.comparing(tool -> tool.getId().value()))
                .toList();
    }

    public MCPTool removeTool(@NotNull ToolId id) {
        lock.writeLock().lock();
        try {
            MCPTool removed = tools.remove(id);
            if (removed == null) {
                throw new IllegalArgumentException("Tool not found: " + id);
            }
            removeFromIndex(displayIndex, removed.getDisplayName(), id);
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public MCPTool removeToolByName(@NotNull String name) {
        return removeTool(new ToolId(name));
    }

    public Set<MCPTool> removeToolByDisplayName(@NotNull String displayName) {
        lock.writeLock().lock();
        try {
            Set<ToolId> ids = displayIndex.remove(displayName);
            if (ids == null || ids.isEmpty()) {
                throw new IllegalArgumentException("Tool not found: " + displayName);
            }

            Set<MCPTool> removed = new LinkedHashSet<>();
            for (ToolId id : ids) {
                MCPTool tool = tools.remove(id);
                if (tool != null) {
                    removed.add(tool);
                }
            }
            return Set.copyOf(removed);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeToolsByServer(@NotNull String serverName) {
        lock.writeLock().lock();
        try {
            tools.values().stream()
                    .filter(tool -> serverName.equals(tool.getInfo().getServerName()))
                    .map(MCPTool::getId)
                    .toList()
                    .forEach(this::removeToolLocked);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeToolLocked(ToolId id) {
        MCPTool removed = tools.remove(id);
        if (removed != null) {
            removeFromIndex(displayIndex, removed.getDisplayName(), id);
        }
    }

    private static void removeFromIndex(Map<String, Set<ToolId>> index, String key, ToolId id) {
        Set<ToolId> ids = index.get(key);
        if (ids != null) {
            ids.remove(id);
            if (ids.isEmpty()) {
                index.remove(key, ids);
            }
        }
    }
}
