package com.bulefire.neuracraft.core.plugin;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginFile {
    private final File file;
    private final JarFile jarFile;
    private final Manifest manifest;

    public PluginFile(@NotNull Path jarFilePath) {
        try {
            this.file = jarFilePath.toFile();
            this.jarFile = new JarFile(jarFilePath.toFile());
            this.manifest = jarFile.getManifest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PluginFile(File file, JarFile jarFile, Manifest manifest) {
        this.file = file;
        this.jarFile = jarFile;
        this.manifest = manifest;
    }

    public Path getFilePath() {
        return this.file.toPath();
    }

    public List<String> getMainClass() {
        try {
            try (FileSystem fs = FileSystems.newFileSystem(file.toPath(), (ClassLoader) null)) {
                Path root = fs.getPath("/");
                try (var files = Files.walk(root)) {
                    return PluginAnnotationScanner.getMainClass(
                            files.filter(Files::isRegularFile)
                                    .filter(file -> file.getFileName().toString().endsWith(".class"))
                                    .map(file -> Utils.readClassFromJar(fs, file))
                                    .toList()
                    ).stream().map(className -> className.replace('/', '.')).toList();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
