package com.bulefire.neuracraft.core.mcp.extern;

import com.bulefire.neuracraft.core.mcp.extern.config.ExternMCPConfig;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.mcp.extern.local.LocalMCPServer;
import com.bulefire.neuracraft.core.mcp.extern.sse.SSEMCPServer;
import com.bulefire.neuracraft.core.mcp.extern.streamablehttp.StreamableHttpMCPServer;
import com.bulefire.neuracraft.core.util.Lifecycle;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ExternMCPController implements Lifecycle {
    private static final ExternMCPController INSTANCE = new ExternMCPController();
    
    public static ExternMCPController getInstance() {
        return INSTANCE;
    }
    
    private ExternMCPController() {}
    
    @Getter
    private ExternMCPConfig config;
    @Getter
    private ExternMCPServerManager serverManager;
    private ExecutorService executor;
    private State state = State.NEW;

    private enum State {
        NEW, STARTING, RUNNING, STOPPING, STOPPED
    }
    
    @Override
    public synchronized void start() {
        if (state == State.STARTING || state == State.RUNNING)
            return;
        state = State.STARTING;
        log.info("ExternMCPController init");
        config = ExternMCPConfig.init();
        serverManager = new ExternMCPServerManager();
        executor = Executors.newFixedThreadPool(Math.max(config.getMcpServers().size(),1));
        
        log.debug("config is {}", config);
        for (Map.Entry<String, ExternMCPConfig.MCPServer> entry : config.getMcpServers().entrySet()) {
            try {
                log.info("Starting mcp server: {} with type {}", entry.getKey(), entry.getValue().getType());
                switch (entry.getValue().getType()) {
                    case "stdio" -> startLocalMCPServer(entry);
                    case "sse" -> startSSEMCPServer(entry);
                    case "streamable-http" -> startStreamableHttpMCPServer(entry);
                    default -> throw new IllegalArgumentException("Unknown mcp server type: " + entry.getValue().getType());
                }
            } catch (RuntimeException e) {
                log.error("Failed to start MCP server {}", entry.getKey(), e);
            }
        }
        state = State.RUNNING;
        log.info("ExternMCPController init end");
    }

    @Override
    public synchronized void stop() {
        if (state == State.NEW || state == State.STOPPING || state == State.STOPPED)
            return;
        state = State.STOPPING;
        log.info("ExternMCPController server stop");
        if (serverManager != null) {
            serverManager.stopAll();
            for (String serverName : serverManager.getServerNames())
                MCPController.getInstance().getMcpManager().removeToolsByServer(serverName);
        }
        if (executor != null) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS))
                    executor.shutdownNow();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
        state = State.STOPPED;
    }
    
    private void startLocalMCPServer(Map.@NotNull Entry<String, ExternMCPConfig.MCPServer> entry) {
        log.info("Starting local mcp server: {} with command {} and arg {}", entry.getKey(), entry.getValue().getCommand(), entry.getValue().getArgs());
        var server = new LocalMCPServer(entry.getKey(), entry.getValue().getCommand(), entry.getValue().getArgs());
        executor.submit(server::start);
        serverManager.registerServer(entry.getKey(), server);
    }
    
    @SneakyThrows
    private void startSSEMCPServer(Map.@NotNull Entry<String, ExternMCPConfig.MCPServer> entry) {
        log.info("Starting sse mcp server: {} with url {}", entry.getKey(), entry.getValue().getUrl());
        RemoteMCPServer server = new SSEMCPServer(entry.getKey(), entry.getValue().getUrl());
        executor.submit(server::start);
        serverManager.registerServer(entry.getKey(), server);
    }
    
    @SneakyThrows
    private void startStreamableHttpMCPServer(Map.@NotNull Entry<String, ExternMCPConfig.MCPServer> entry) {
        log.info("Starting streamable-http mcp server: {} with url {}", entry.getKey(), entry.getValue().getUrl());
        RemoteMCPServer server = new StreamableHttpMCPServer(entry.getKey(), entry.getValue().getUrl());
        executor.submit(server::start);
        serverManager.registerServer(entry.getKey(), server);
    }
}
