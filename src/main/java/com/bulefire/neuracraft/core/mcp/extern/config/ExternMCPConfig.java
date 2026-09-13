package com.bulefire.neuracraft.core.mcp.extern.config;

import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.config.ConfigValidation;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ExternMCPConfig {
    private Map<String, MCPServer> mcpServers = new HashMap<>(1);
    
    @SneakyThrows
    public static @NotNull ExternMCPConfig init() {
        Path configPath = FileUtil.mcp_config_url.resolve("config.json");
        if (! configPath.toFile().exists())
            FileUtil.saveJsonToFile(new ExternMCPConfig(), configPath);
        ExternMCPConfig config = FileUtil.loadJsonFromFile(configPath, ExternMCPConfig.class);
        config.validate();
        return config;
    }

    public void validate() {
        if (mcpServers == null)
            mcpServers = new HashMap<>();
        for (Map.Entry<String, MCPServer> entry : mcpServers.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank())
                throw new IllegalArgumentException("MCP server name must not be blank");
            MCPServer server = entry.getValue();
            if (server == null || server.getType() == null)
                throw new IllegalArgumentException("MCP server " + entry.getKey() + " is invalid");
            switch (server.getType()) {
                case "stdio" -> ConfigValidation.requireText(server.getCommand(), "command");
                case "sse", "streamable-http" -> ConfigValidation.requireHttpUrl(server.getUrl(), "url");
                default -> throw new IllegalArgumentException("Unknown MCP server type: " + server.getType());
            }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MCPServer {
        @Builder.Default
        private String type = "";
        @Builder.Default
        private String command = "";
        @Builder.Default
        private List<String> args = new ArrayList<>();
        @Builder.Default
        private String url = "";
        
        public URL getURLasURL() {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
