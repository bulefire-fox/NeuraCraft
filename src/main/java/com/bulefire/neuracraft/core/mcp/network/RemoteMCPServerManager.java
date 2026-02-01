package com.bulefire.neuracraft.core.mcp.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteMCPServerManager {
    private final ConcurrentMap<String, RemoteMCPServer> servers;
    private final ConcurrentMap<String, String> toolMthodToServer;
    
    public RemoteMCPServerManager() {
        servers = new ConcurrentHashMap<>();
        toolMthodToServer = new ConcurrentHashMap<>();
    }
    
    public void registerServer(@NotNull String serverName, @NotNull RemoteMCPServer server) {
        servers.put(serverName, server);
        var tools = server.getAliveTools();
        for (var tool : tools) {
            toolMthodToServer.put(tool.getInfo().getMethod(), serverName);
        }
    }
    
    public void deleteServer(@NotNull String serverName) {
        servers.remove(serverName);
    }
    
    public @NotNull RemoteMCPServer getServer(@NotNull String serverName) {
        return servers.get(serverName);
    }
    public @NotNull Collection<RemoteMCPServer> getAllAliveServer() {
        return servers.values().stream().filter(RemoteMCPServer::isAlive).toList();
    }
    
    public @Nullable RemoteMCPServer getServerByTool(@NotNull String toolMethodName) {
        var serverName = toolMthodToServer.get(toolMethodName);
        return servers.get(serverName);
    }
}
