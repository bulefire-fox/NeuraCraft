package com.bulefire.neuracraft.core.config;

import com.bulefire.neuracraft.compatibility.util.FileUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NCMainConfig {
    public static final Path mainConfigPath = FileUtil.base_url.resolve("neuracraft.json");
    public static final File mainConfigFile = mainConfigPath.toFile();

    public static void init() {
        if (! mainConfigFile.exists()) {
            try {
                mainConfigFile.createNewFile();
                var neuraCraft = new NeuraCraft();
                neuraCraft.setPrefix("AI");
                neuraCraft.setMcpServers(List.of());
                FileUtil.saveJsonToFile(neuraCraft, mainConfigPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        load();
    }

    @Getter
    private static String prefix = "AI";
    
    @Getter
    private static List<String> mcpServers = new ArrayList<>();

    @SneakyThrows
    public static void load() {
        NeuraCraft neuraCraft = FileUtil.loadJsonFromFile(mainConfigPath, NeuraCraft.class);
        prefix = neuraCraft.getPrefix();
        mcpServers = neuraCraft.getMcpServers();
    }

    @Getter
    @Setter
    public static class NeuraCraft {
        private String prefix;
        private List<String> mcpServers;
    }
}
