package com.bulefire.neuracraft.core.mcp.network;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class RemoteMCPServerManager {
    private final ConcurrentMap<String, RemoteMCPServer> servers;
    private final ConcurrentMap<String, String> toolToServer;
    
    public RemoteMCPServerManager() {
        servers = new ConcurrentHashMap<>();
        toolToServer = new ConcurrentHashMap<>();
    }
    
    public void registerServer(@NotNull String serverName, @NotNull RemoteMCPServer server) {
        servers.put(serverName, server);
    }
    
    public void deleteServer(@NotNull String serverName) {
        servers.remove(serverName);
    }
    
    public void registerTool(@NotNull String toolName, @NotNull String serverName) {
        toolToServer.put(toolName, serverName);
    }
    
    public void deleteTool(@NotNull String toolName) {
        toolToServer.remove(toolName);
    }
    
    public @NotNull RemoteMCPServer getServer(@NotNull String serverName) {
        return servers.get(serverName);
    }
    public @NotNull Collection<RemoteMCPServer> getAllAliveServer() {
        return servers.values().stream().filter(RemoteMCPServer::isAlive).toList();
    }
    
    public @NotNull RemoteMCPServer getServerByTool(@NotNull String toolName) {
        var serverName = toolToServer.get(toolName);
        return servers.get(serverName);
    }
}
