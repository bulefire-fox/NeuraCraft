package com.bulefire.neuracraft.core.plugin;

import com.bulefire.neuracraft.compatibility.util.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Log4j2
public class PluginScanner {
    public static @NotNull @Unmodifiable List<Path> scanPlugins() throws IOException {
        return scanPlugins(FileUtil.plugin_url);
    }

    public static @NotNull @Unmodifiable List<Path> scanPlugins(String scanpath) throws IOException {
        return scanPlugins(Path.of(scanpath));
    }

    public static @NotNull @Unmodifiable List<Path> scanPlugins(Path scanpath) throws IOException {
        log.debug("Scanning plugins in {}", scanpath);
        try (var files = Files.walk(scanpath)){
            return files.filter(Files::isRegularFile)
                    .peek(file -> log.debug("Checking file {}", file.getFileName().toString()))
                    .filter(file -> file.getFileName().toString().endsWith(".jar"))
                    .peek(file -> log.debug("Found maybe plugin file {}", file))
                    .filter(PluginScanner::isPlugin)
                    .peek(file -> log.debug("Found plugin file {}", file))
                    .toList();
        }
    }

    public static boolean isPlugin(@NotNull Path path) {
        log.debug("Checking jar file {}", path);
        try {
            try (FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
                Path root = fs.getPath("/");
                try (var files = Files.walk(root)) {
                    long count = files.filter(Files::isRegularFile)
                            .peek(file -> log.debug("Checking class file {}", file))
                            .filter(file -> file.getFileName().toString().endsWith(".class"))
                            .peek(file -> log.debug("Found class file {}", file))
                            .filter(f -> PluginAnnotationScanner.hasAnnotationWithoutThrows(Utils.readClassFromJar(fs, f), Plugin.class))
                            .peek(file -> log.debug("Found plugin class file {}", file))
                            .count();
                    log.debug("Found {} plugin class files in {}", count, path);
                    return path.getFileName().toString().endsWith(".jar") && count > 0;
                }
            }
        } catch (IOException e) {
            log.error("Error while checking jar file {}", path, e);
            return false;
        }
    }
}
