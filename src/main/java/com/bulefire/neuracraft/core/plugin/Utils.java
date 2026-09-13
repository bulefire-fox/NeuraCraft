package com.bulefire.neuracraft.core.plugin;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class Utils {
    @SneakyThrows
    public static byte @NotNull [] readClassFromJar(@NotNull FileSystem fs, @NotNull Path classPath) {
        Path fullPath = fs.getPath(classPath.toString());
        return Files.readAllBytes(fullPath);
    }
}
