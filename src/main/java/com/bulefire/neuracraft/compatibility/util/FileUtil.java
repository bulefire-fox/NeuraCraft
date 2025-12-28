package com.bulefire.neuracraft.compatibility.util;

import com.bulefire.neuracraft.NeuraCraft;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    public static void init() {
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

    /**
     * 序列化对象到json文件, 如果文件路径不存在则创建
     *
     * @param data     要保存的对象, 可以是任意类型. 字段可以为 {@code final}
     * @param filePath 文件路径, 当前文件系统的路径
     * @throws IOException IOException
     * @apiNote 不序列化内部类, {@code static} 和 {@code transient} 字段, 使用 {@code Gson}
     * @author bulefire_fox
     * @see Gson
     * @see GsonBuilder#disableInnerClassSerialization()
     * @since 1.0
     */
    public static void saveJsonToFile(@NotNull Object data, @NotNull Path filePath) throws IOException {
        log.info("file path: {}", filePath);
        if (!Files.exists(filePath)) {
            log.info("create file: {}", filePath);
            filePath.toFile().getParentFile().mkdirs();
            Files.createFile(filePath);
        }
        Gson g = new GsonBuilder()
                .disableInnerClassSerialization()
                .create();
        try (FileWriter writer = new FileWriter(filePath.toFile().getPath())) {
            log.info("write to file: {}", filePath);
            g.toJson(data, writer);
        } catch (JsonIOException e) {
            log.error("JsonIOException: {}", e.getMessage());
        }
    }

    /**
     * 从 {@code json} 文件中反序列化对象, 返回一个新的 {@link T} 类型的对象.
     *
     * @param filePath {@code json} 文件的路径
     * @param clazz    反序列化的对象类型, 及 {@link Class } 对象
     * @param <T>      反序列化的对象类型
     * @return 一个新的 {@link T} 类型的对象
     * @throws IOException          IOException
     * @throws NullPointerException NullPointerException
     * @author bulefire_fox
     * @apiNote 使用 {@code Gson} 反序列化, 无法处理 {@code final} {@code static} {@code transient} 字段
     * @see Gson
     * @since 1.0
     */
    public static <T> @NotNull T loadJsonFromFile(@NotNull Path filePath, Class<T> clazz) throws IOException {
        T t;
        try (FileReader reader = new FileReader(filePath.toFile())) {
            t = new GsonBuilder().setPrettyPrinting().create().fromJson(reader, clazz);
        }

        if (!(t == null)) {
            return t;
        }
        throw new NullPointerException("load json from file failed");
    }

    /**
     * 从 {@code json} 文件中反序列化对象, 直接修改传入的 {@link T} 对象
     *
     * @param filePath {@code json} 文件的路径
     * @param clazz    反序列化时需要直接修改的 {@link T} 对象
     * @throws IOException          IOException
     * @throws NullPointerException NullPointerException
     * @author bulefire_fox
     * @apiNote 使用 {@code Gson} 反序列化, 无法处理 {@code final} {@code static} {@code transient} 字段
     * @see Gson
     * @see FileUtil#shouldProcess(Field)
     * @since 2.0
     */
    @SuppressWarnings("unchecked")
    public static <T> void loadJsonFromFile(@NotNull Path filePath, @NotNull T clazz) throws IOException {
        T other = (T) loadJsonFromFile(filePath, clazz.getClass());
        Field[] fields = clazz.getClass().getDeclaredFields();
        for (Field field : fields) {
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

    /**
     * 读取指定目录下的所有存在的文件的路径 <br>
     * 等价于 ( 省略try-with-resource语句 ) :
     * <pre>
     *     {@code
     *        Files.walk(baseURL)
     *           .filter(Files::isRegularFile)
     *           .toList();
     *     }
     * </pre>
     *
     * @param baseURL 指定的文件根目录
     * @return 所有文件路径组成的 {@link List}
     * @throws IOException IOException
     * @author bulefire_fox
     * @apiNote 使用 {@link Files#walk(Path, FileVisitOption...)} 实现, 推荐直接使用 {@link Files#walk(Path, FileVisitOption...)}
     * 而不是此方法以获取深入的定制化 {@link Stream}
     * @see Files#walk(Path, FileVisitOption...)
     * @since 1.0
     */
    public static @NotNull @Unmodifiable List<Path> readAllFilePath(Path baseURL) throws IOException {
        try (Stream<Path> paths = Files.walk(baseURL)) {
            return paths
                    .filter(Files::isRegularFile)
                    .toList();
        }
    }
}
