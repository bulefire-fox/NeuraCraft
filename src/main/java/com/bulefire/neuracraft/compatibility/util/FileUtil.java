package com.bulefire.neuracraft.compatibility.util;

import com.bulefire.neuracraft.NeuraCraft;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import lombok.extern.log4j.Log4j2;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class FileUtil {

    /**
     * 配置文件路径
     */
    public static final Path base_url = NeuraCraft.configPath.resolve("neuracraft");
    /**
     * Agent 文件根目录
     */
    public static final Path agent_base_url = base_url.resolve("agent");
    /**
     * Agent 配置文件根目录
     */
    public static final Path agent_config_url = agent_base_url.resolve("config");
    /**
     * 玩家配置文件根目录
     */
    public static final Path player_url = base_url.resolve("player");

    /**
     * 插件目录
     */
    public static final Path plugin_url = base_url.resolve("plugin");

    public static void init(){
        // 创建不存在的目录
        if (!base_url.toFile().exists())
            base_url.toFile().mkdirs();
        if (!agent_base_url.toFile().exists())
            agent_base_url.toFile().mkdirs();
        if (!player_url.toFile().exists())
            player_url.toFile().mkdirs();
        if (!plugin_url.toFile().exists())
            plugin_url.toFile().mkdirs();
        log.debug("init file util done");
    }

    public static void saveJsonToFile(@NotNull Object data, @NotNull Path filePath) throws IOException {
        log.info("file path: {}", filePath);
        if (!Files.exists(filePath)){
            log.info("create file: {}", filePath);
            filePath.toFile().getParentFile().mkdirs();
            Files.createFile(filePath);
        }
        Gson g = new GsonBuilder()
                .disableInnerClassSerialization()
                .create();
        try (FileWriter writer = new FileWriter(filePath.toFile().getPath())){
            log.info("write to file: {}", filePath);
            g.toJson(data, writer);
        } catch (JsonIOException e) {
            log.error("JsonIOException: {}", e.getMessage());
        }
    }

    public static <T> @NotNull T loadJsonFromFile(@NotNull Path filePath, Class<T> clazz) throws IOException{
        T t;
        try (FileReader reader = new FileReader(filePath.toFile())){
            t = new GsonBuilder().setPrettyPrinting().create().fromJson(reader, clazz);
        }

        if (!(t == null)){
            return t;
        }
        throw new NullPointerException("load json from file failed");
    }

    @SuppressWarnings("unchecked")
    public static <T> void loadJsonFromFile(@NotNull Path filePath, @NotNull T clazz) throws IOException{
        T other = (T) loadJsonFromFile(filePath, clazz.getClass());
        Field [] fields = clazz.getClass().getDeclaredFields();
        for (Field field : fields){
            if (!shouldProcess(field)) continue;
            field.setAccessible(true);
            try {
                log.debug("load {}'s field {} from {} value {}", clazz, field.getName(), other, field.get(other));
                field.set(clazz, field.get(other));
            } catch (IllegalAccessException e) {
                 log.error("IllegalAccessException: {}", e.getMessage());
                 throw new RuntimeException(e);
            }
        }
    }

    private static boolean shouldProcess(@NotNull Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers)
                && !Modifier.isFinal(modifiers)
                && !Modifier.isTransient(modifiers);
    }

    public static @NotNull @Unmodifiable List<Path> readAllFilePath(Path baseURL) throws IOException {
        try (Stream<Path> paths = Files.walk(baseURL)){
            return paths
                    .filter(Files::isRegularFile)
                    .toList();
        }
    }
}
