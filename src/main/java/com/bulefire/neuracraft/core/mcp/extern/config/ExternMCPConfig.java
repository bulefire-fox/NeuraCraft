package com.bulefire.neuracraft.core.mcp.extern.config;

import com.bulefire.neuracraft.compatibility.util.FileUtil;
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
    private Map<String, MCPServer> mcpServers = new HashMap<>();
    
    @SneakyThrows
    public static @NotNull ExternMCPConfig init() {
        Path configPath = FileUtil.mcp_config_url.resolve("config.json");
        if (! configPath.toFile().exists())
            FileUtil.saveJsonToFile(new ExternMCPConfig(), configPath);
        return FileUtil.loadJsonFromFile(configPath, ExternMCPConfig.class);
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
