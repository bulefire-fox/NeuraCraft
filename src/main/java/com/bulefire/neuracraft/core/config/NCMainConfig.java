package com.bulefire.neuracraft.core.config;

import com.bulefire.neuracraft.compatibility.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class NCMainConfig {
    public static final Path mainConfigPath = FileUtil.base_url.resolve("neuracraft.json");
    public static final File mainConfigFile = mainConfigPath.toFile();
    public static void init(){
        if (!mainConfigFile.exists()) {
            try {
                mainConfigFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
