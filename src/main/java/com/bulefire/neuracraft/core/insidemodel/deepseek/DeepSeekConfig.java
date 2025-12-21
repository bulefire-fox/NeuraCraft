package com.bulefire.neuracraft.core.insidemodel.deepseek;

import com.bulefire.neuracraft.compatibility.util.FileUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.nio.file.Path;


public class DeepSeekConfig {
    private static final Path configPath = FileUtil.agent_base_url.resolve("deepseek/deepseek.json");

    @Getter
    private static String modelName;
    @Getter
    private static String displayName;
    @Getter
    private static int timePerMin;
    @Getter
    private static String url;
    @Getter
    private static String token;

    @SneakyThrows
    public static void init(){
        if (!configPath.toFile().exists()) {
            configPath.toFile().getParentFile().mkdirs();
            configPath.toFile().createNewFile();
            FileUtil.saveJsonToFile(new Config(), configPath);
        }
        load();
    }

    @SneakyThrows
    public static void load(){
        Config config = FileUtil.loadJsonFromFile(configPath, Config.class);
        modelName = config.getModelName();
        displayName = config.getDisplayName();
        timePerMin = config.getTimePerMin();
        url = config.getUrl();
        token = config.getToken();
    }

    @Getter
    @Setter
    public static class Config {
        private String modelName;
        private String displayName;
        private int timePerMin;
        private String url;
        private String token;

        public Config(){
            modelName = "deepseek-reasoner";
            displayName = "DeepSeek";
            timePerMin = 60;
            url = "https://api.deepseek.com/chat/completions";
            token = "";
        }
    }
}
