package com.bulefire.neuracraft.core.plugin;

import com.bulefire.neuracraft.core.agent.AgentController;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class PluginContainer implements AutoCloseable {
    private final PluginFile pluginFile;
    private final PluginClassLoader classLoader;
    private final List<Class<?>> mainClasses;
    private final List<Object> mainInstances;
    private final Map<Class<? extends Annotation>, RegisteredClassScannerData> registeredData;

    @SneakyThrows
    public PluginContainer(@NotNull PluginFile pluginFile) {
        this.pluginFile = pluginFile;
        this.classLoader = new PluginClassLoader(new URL[]{pluginFile.getFilePath().toUri().toURL()}, AgentController.class.getClassLoader());
        this.mainClasses = new ArrayList<>(1);
        this.mainInstances = new ArrayList<>(1);
        for (String className : pluginFile.getMainClass()) {
            try {
                this.mainClasses.add(classLoader.loadClass(className));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        this.registeredData = new HashMap<>();
    }

    public void load() {
        instanceMainClasses();
        scanRegisteredClasses(PluginLoader.PLUGIN_ANNOTATIONS);
    }

    public void instanceMainClasses() {
        for (Class<?> clazz : mainClasses) {
            try {
                log.debug("Instance main class {}", clazz.getName());
                Object instance = clazz.getConstructor().newInstance();
                mainInstances.add(instance);
                if (instance instanceof PluginLifecycle lifecycle) {
                    PluginDescriptor descriptor = pluginFile.getDescriptor();
                    lifecycle.initialize(new PluginContext(
                            descriptor == null ? clazz.getName() : descriptor.id(),
                            pluginFile.getFilePath(),
                            descriptor == null ? "legacy" : descriptor.apiVersion()
                    ));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void scanRegisteredClasses(@NotNull List<Class<? extends Annotation>> annotations) {
        for (Class<? extends Annotation> annotation : annotations) {
            Target target = annotation.getAnnotation(Target.class);
            if (target == null || target.value().length == 0)
                continue;
            ElementType[] elementTypes = target.value();
            for (ElementType elementType : elementTypes) {
                switch (elementType) {
                    case TYPE -> {
                        List<Class<?>> classes;
                        try (FileSystem fs = FileSystems.newFileSystem(pluginFile.getFilePath(), (ClassLoader) null)) {
                            Path root = fs.getPath("/");
                            try (var files = Files.walk(root)) {
                                classes = PluginAnnotationScanner.scanClassAnnotations(
                                                                         annotation,
                                                                         files.filter(Files::isRegularFile)
                                                                              .filter(file -> file.getFileName().toString().endsWith(".class"))
                                                                              .map(file -> Utils.readClassFromJar(fs, file))
                                                                              .toList()
                                                                 ).stream()
                                                                 .map(className -> className.replace('/', '.'))
                                                                 .map(className -> {
                                                                     try {
                                                                         return classLoader.loadClass(className);
                                                                     } catch (ClassNotFoundException e) {
                                                                         throw new RuntimeException(e);
                                                                     }
                                                                 }).collect(Collectors.toUnmodifiableList());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        var data = new RegisteredClassScannerData(annotation);
                        data.addClasses(classes);
                        registeredData.put(annotation, data);
                    }
                    case METHOD -> {
                        List<Method> methods;
                        try (FileSystem fs = FileSystems.newFileSystem(pluginFile.getFilePath(), (ClassLoader) null)) {
                            Path root = fs.getPath("/");
                            try (var files = Files.walk(root)) {
                                methods = PluginAnnotationScanner.scanClassMethodsAnnotations(
                                        annotation,
                                        files.filter(Files::isRegularFile)
                                             .filter(file -> file.getFileName().toString().endsWith(".class"))
                                             .map(file -> Utils.readClassFromJar(fs, file))
                                             .toList(),
                                        classLoader
                                );
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        var data = new RegisteredClassScannerData(annotation);
                        data.addMethods(methods);
                        registeredData.put(annotation, data);
                    }
                    default -> log.warn("Annotation {} is not supported", annotation.getName());
                }
            }
        }
    }

    public RegisteredClassScannerData getRegisteredData(@NotNull Class<? extends Annotation> annotation) {
        return registeredData.get(annotation);
    }

    @Override
    public void close() {
        for (int i = mainInstances.size() - 1; i >= 0; i--) {
            Object instance = mainInstances.get(i);
            if (instance instanceof PluginLifecycle lifecycle) {
                try {
                    lifecycle.shutdown();
                } catch (RuntimeException e) {
                    log.warn("Plugin shutdown failed for {}", instance.getClass().getName(), e);
                }
            }
        }
        try {
            classLoader.close();
            pluginFile.close();
        } catch (IOException e) {
            log.warn("Failed to close plugin resources for {}", pluginFile.getFilePath(), e);
        }
    }
}
