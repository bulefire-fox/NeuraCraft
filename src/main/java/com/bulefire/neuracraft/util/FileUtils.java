package com.bulefire.neuracraft.util;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.config.opa.OPA;
import com.bulefire.neuracraft.config.opa.OPAEntity;
import com.bulefire.neuracraft.config.yy.BaseInformation;
import com.bulefire.neuracraft.config.yy.BaseInformationEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    private static final Logger logger = LogUtils.getLogger();

    public static final Path configPath = NeuraCraft.configPath.resolve("neuracraft");
    public static final Path chatPath = configPath.resolve("chat");
    public static final Path playerPath = configPath.resolve("player");
    public static final Path modulePath = configPath.resolve("module");

    public static void init() throws IOException{
        logger.info("init file utils");
        if (!Files.exists(configPath))
            Files.createDirectory(configPath);

        if (!Files.exists(chatPath))
            Files.createDirectory(chatPath);

        if (!Files.exists(playerPath))
            Files.createDirectory(playerPath);

        if (!Files.exists(modulePath))
            Files.createDirectory(modulePath);

        logger.info("init multi module {} {}", BaseInformation.enable_multi_module, OPA.enable_multi_module);

        if (BaseInformation.enable_multi_module)
            saveJsonToFile(new BaseInformationEntity(), modulePath.resolve("CyberFurry.json"));

        if (OPA.enable_multi_module)
            saveJsonToFile(new OPAEntity(), modulePath.resolve("OPA.json"));
    }

    public static void saveJsonToFile(@NotNull Object data, @NotNull Path filePath) throws IOException {
        logger.info("file path: {}", filePath);
        if (!Files.exists(filePath)){
            logger.info("create file: {}", filePath);
            Files.createFile(filePath);
        }
        Gson g = new GsonBuilder()
                .disableInnerClassSerialization()
                .create();
        try (FileWriter writer = new FileWriter(filePath.toFile().getPath())){
            logger.info("write to file: {}", filePath);
            g.toJson(data, writer);
        } catch (JsonIOException e) {
            logger.error("JsonIOException: {}", e.getMessage());
        }
    }

    public static <T> @NotNull T loadJsonFromFile(@NotNull Path filePath, Class<T> clazz) throws IOException{
        T t;
        try (FileReader reader = new FileReader(filePath.toFile())){
            t = new Gson().fromJson(reader, clazz);
        }

        if (!(t == null)){
            return t;
        }
        throw new NullPointerException("load json from file failed");
    }

    public static List<Path> readAllFilePath(Path baseURL) throws IOException {
        try (Stream<Path> paths = Files.list(baseURL)){
            return paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
    }
}
