package com.bulefire.neuracraft.core.mcp.extern;

import com.bulefire.neuracraft.compatibility.function.process.LevelUnloadEventProcess;
import com.bulefire.neuracraft.compatibility.function.process.ServerStoppingEventProcesser;
import com.bulefire.neuracraft.core.mcp.extern.config.ExternMCPConfig;
import com.bulefire.neuracraft.core.mcp.extern.local.LocalMCPServer;
import com.bulefire.neuracraft.core.mcp.extern.sse.SSEMCPServer;
import com.bulefire.neuracraft.core.mcp.extern.streamablehttp.StreamableHttpMCPServer;
import io.modelcontextprotocol.json.schema.jackson.JacksonJsonSchemaValidatorSupplier;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Log4j2
public class ExternMCPController {
    private static final ExternMCPController INSTANCE = new ExternMCPController();
    
    public static ExternMCPController getInstance() {
        return INSTANCE;
    }
    
    private ExternMCPController() {}
    
    @Getter
    private ExternMCPConfig config;
    @Getter
    private ExternMCPServerManager serverManager;
    private static ExecutorService executor;
    
    public void init() {
        log.info("ExternMCPController init");
        config = ExternMCPConfig.init();
        serverManager = new ExternMCPServerManager();
        executor = Executors.newFixedThreadPool(config.getMcpServers().size());
        
        // fuck mcp-java-sdk
        // fuck forge
        // fuck serviceloader
        // I like java reflection (～￣▽￣)～
        // don't remove it in jdk21 (although this has already happened /_ \)
        try {
            // 暴力设置 JsonSchemaInternal.defaultValidator 字段
            // serviceLoader 被forge干废力 (悲)
            Class<?> jsonSchemaInternal = Class.forName("io.modelcontextprotocol.json.schema.JsonSchemaInternal");
            Field defaultValidator = jsonSchemaInternal.getDeclaredField("defaultValidator");
            defaultValidator.setAccessible(true);
            defaultValidator.set(null, (new JacksonJsonSchemaValidatorSupplier()).get());
            log.debug("JsonSchemaInternal.defaultValidator set to {}", defaultValidator.get(null));
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            // 你问我兼容性怎么办?
            // 我sdk都是手动构建塞进去的你和我说兼容性? (
            // 我都用上反射了还在乎这些东西吗 (
            // 我真没招了
            throw new RuntimeException(e);
        }
        
        log.debug("config is {}", config);
        for (Map.Entry<String, ExternMCPConfig.MCPServer> entry : config.getMcpServers().entrySet()) {
            log.info("Starting mcp server: {} with type {}", entry.getKey(), entry.getValue().getType());
            switch (entry.getValue().getType()) {
                case "stdio" -> startLocalMCPServer(entry);
                case "sse" -> startSSEMCPServer(entry);
                case "streamable-http" -> startStreamableHttpMCPServer(entry);
                default -> throw new IllegalArgumentException("Unknown mcp server type: " + entry.getValue().getType());
            }
        }
        
        ServerStoppingEventProcesser.registerFun(() -> {
            log.info("ExternMCPController server stop");
            serverManager.stopAll();
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // 强制关闭
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        });
        
        LevelUnloadEventProcess.registerFun(() -> {
            log.info("ExternMCPController level stop");
            serverManager.stopAll();
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // 强制关闭
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        });
        log.info("ExternMCPController init end");
    }
    
    private void startLocalMCPServer(Map.@NotNull Entry<String, ExternMCPConfig.MCPServer> entry) {
        log.info("Starting local mcp server: {} with command {} and arg {}", entry.getKey(), entry.getValue().getCommand(), entry.getValue().getArgs());
        var server = new LocalMCPServer(entry.getKey(), entry.getValue().getCommand(), entry.getValue().getArgs());
        executor.submit(() -> {
            try {
                server.start();
            } catch (Throwable e) {
                Throwable cause = e instanceof RuntimeException ? e.getCause() : e;
                if (cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError) {
                    log.error("MCP 启动失败: 找不到 MCP 类，请确保 build.gradle 中已添加 implementation 依赖。", e);
                } else {
                    log.error("MCP 本地服务器 {} 启动失败", entry.getKey(), e);
                }
            }
        });
        serverManager.registerServer(entry.getKey(), server);
    }
    
    @SneakyThrows
    private void startSSEMCPServer(Map.@NotNull Entry<String, ExternMCPConfig.MCPServer> entry) {
        log.info("Starting sse mcp server: {} with command {} and arg {}", entry.getKey(), entry.getValue().getCommand(), entry.getValue().getArgs());
        RemoteMCPServer server = new SSEMCPServer(entry.getKey(), entry.getValue().getUrl());
        log.debug("LocalMCPServer: {}", server);
        executor.submit(server::start);
        serverManager.registerServer(entry.getKey(), server);
    }
    
    @SneakyThrows
    private void startStreamableHttpMCPServer(Map.@NotNull Entry<String, ExternMCPConfig.MCPServer> entry) {
        log.info("Starting streamable-http mcp server: {} with command {} and arg {}", entry.getKey(), entry.getValue().getCommand(), entry.getValue().getArgs());
        RemoteMCPServer server = new StreamableHttpMCPServer(entry.getKey(), entry.getValue().getUrl());
        executor.submit(server::start);
        serverManager.registerServer(entry.getKey(), server);
    }
}
