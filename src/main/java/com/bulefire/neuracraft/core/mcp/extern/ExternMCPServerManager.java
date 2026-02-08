package com.bulefire.neuracraft.core.mcp.extern;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class ExternMCPServerManager {
    private final Map<String, RemoteMCPServer> servers = new HashMap<>();
    
    public void registerServer(String name, RemoteMCPServer server) {
        servers.put(name, server);
    }
    
    public RemoteMCPServer getServer(String name) {
        return servers.get(name);
    }
    
    public void stopAll() {
        log.info("Stopping all mcp servers");
        servers.values().forEach(RemoteMCPServer::stop);
    }
}
