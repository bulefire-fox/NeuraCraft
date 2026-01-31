package com.bulefire.neuracraft.core.mcp.network;

import lombok.Getter;

public class RemoteMCPServerController {
    private static final RemoteMCPServerController INSTANCE = new RemoteMCPServerController();
    private RemoteMCPServerController() {
        serverManager = new RemoteMCPServerManager();
    }
    public static RemoteMCPServerController getInstance() {return INSTANCE;}
    
    @Getter
    private final RemoteMCPServerManager serverManager;
    
    public void initializeAllRemoteServer() {
        serverManager.getAllAliveServer().forEach(RemoteMCPServer::initialize);
    }
}
