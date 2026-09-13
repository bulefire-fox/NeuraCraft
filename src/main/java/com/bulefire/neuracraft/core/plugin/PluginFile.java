package com.bulefire.neuracraft.core.plugin;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginFile implements AutoCloseable {
    public static final String DESCRIPTOR_PATH = "META-INF/neuracraft-plugin.json";
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
        PluginDescriptor descriptor = getDescriptor();
        if (descriptor != null && descriptor.mainClass() != null && !descriptor.mainClass().isBlank())
            return List.of(descriptor.mainClass());
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

    public PluginDescriptor getDescriptor() {
        try {
            var entry = jarFile.getJarEntry(DESCRIPTOR_PATH);
            if (entry == null)
                return null;
            try (var reader = new InputStreamReader(jarFile.getInputStream(entry), StandardCharsets.UTF_8)) {
                return PluginDescriptor.read(reader);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read plugin descriptor from " + file, e);
        }
    }

    @Override
    public void close() throws IOException {
        jarFile.close();
    }
}
