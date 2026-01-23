package com.bulefire.neuracraft.core.plugin;

import com.bulefire.neuracraft.compatibility.util.scanner.AnnotationsMethodScanner;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PluginAnnotationScanner {
    public static boolean hasAnnotationWithoutThrows(Path klass, Class<? extends Annotation> annotation) {
        try {
            return hasAnnotation(klass, annotation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasAnnotationWithoutThrows(byte[] klass, Class<? extends Annotation> annotation) {
        return hasAnnotation(klass, annotation);
    }

    public static boolean hasAnnotation(Path klass, Class<? extends Annotation> annotation) throws IOException {
        return hasAnnotation(Files.readAllBytes(klass), annotation);
    }

    public static boolean hasAnnotation(byte[] klass, Class<? extends Annotation> annotation) {
        ClassReader reader = new ClassReader(klass);
        Set<String> annotations = new HashSet<>();
        reader.accept(new PluginAnnotationsClassVisitor(null, annotations), 2);
        return annotations.contains(Type.getDescriptor(annotation));
    }

    private static class PluginAnnotationsClassVisitor extends ClassVisitor {
        private final Set<String> annotations;

        public PluginAnnotationsClassVisitor(ClassVisitor classVisitor, Set<String> annotations) {
            super(Opcodes.ASM9, classVisitor);
            this.annotations = annotations;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            annotations.add(descriptor);
            return super.visitAnnotation(descriptor, visible);
        }
    }

    @SneakyThrows
    public static @NotNull List<String> getMainClassByPath(@NotNull List<Path> classes) {
        return getMainClass(classes.stream().map(PluginAnnotationScanner::readAllBytes).toList());
    }

    public static @NotNull List<String> getMainClass(@NotNull List<byte[]> classes) {
        List<String> result = new ArrayList<>();
        for (byte[] klass : classes) {
            ClassReader reader = new ClassReader(klass);
            reader.accept(new PluginMainClassVisitor(null, result), 2);
        }
        return result;
    }

    private static class PluginMainClassVisitor extends ClassVisitor {
        private final List<String> result;
        private String className;

        public PluginMainClassVisitor(ClassVisitor classVisitor, List<String> result) {
            super(Opcodes.ASM9, classVisitor);
            this.result = result;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (descriptor.equals(Type.getDescriptor(Plugin.class)))
                result.add(className);
            return super.visitAnnotation(descriptor, visible);
        }
    }

    @SneakyThrows
    public static @NotNull List<String> scanClassAnnotations(@NotNull Class<? extends Annotation> annotation, @NotNull Path jarPath) {
        try (var walk = Files.walk(jarPath)) {
            return scanClassAnnotationsByPath(annotation, walk.toList());
        }
    }

    @SneakyThrows
    public static @NotNull List<String> scanClassAnnotationsByPath(@NotNull Class<? extends Annotation> annotation, @NotNull List<Path> classes) {
        return scanClassAnnotations(annotation, classes.stream().map(PluginAnnotationScanner::readAllBytes).toList());
    }

    @SneakyThrows
    public static @NotNull List<String> scanClassAnnotations(@NotNull Class<? extends Annotation> annotation, @NotNull List<byte[]> classes) {
        List<String> result = new ArrayList<>();
        for (byte[] klass : classes) {
            ClassReader reader = new ClassReader(klass);
            reader.accept(new PluginClassAnnotationVisitor(null, result, annotation), 2);
        }
        return result;
    }

    private static class PluginClassAnnotationVisitor extends ClassVisitor {
        private final List<String> result;
        private String className;
        private final Class<? extends Annotation> annotation;

        public PluginClassAnnotationVisitor(ClassVisitor classVisitor, List<String> result, Class<? extends Annotation> annotation) {
            super(Opcodes.ASM9, classVisitor);
            this.result = result;
            this.annotation = annotation;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(@NotNull String descriptor, boolean visible) {
            if (descriptor.equals(Type.getDescriptor(this.annotation)))
                result.add(className);
            return super.visitAnnotation(descriptor, visible);
        }
    }

    @SneakyThrows
    public static @NotNull List<Method> scanClassMethodsAnnotations(@NotNull Class<? extends Annotation> annotation, @NotNull Path jarPath, ClassLoader classLoader) {
        try (var walk = Files.walk(jarPath)) {
            return scanClassMethodsAnnotationsByPath(annotation, walk.toList(), classLoader);
        }
    }

    @SneakyThrows
    public static @NotNull List<Method> scanClassMethodsAnnotationsByPath(@NotNull Class<? extends Annotation> annotation, @NotNull List<Path> classes, ClassLoader classLoader) {
        return scanClassMethodsAnnotations(annotation, classes.stream().map(PluginAnnotationScanner::readAllBytes).toList(), classLoader);
    }

    @SneakyThrows
    public static @NotNull List<Method> scanClassMethodsAnnotations(@NotNull Class<? extends Annotation> annotation, @NotNull List<byte[]> classes, ClassLoader classLoader) {
        List<Method> result = new ArrayList<>();
        for (byte[] klass : classes) {
            result.addAll(AnnotationsMethodScanner.scannerClass(klass, Collections.singleton(annotation), classLoader));
        }
        return result;
    }

    @SneakyThrows
    private static byte @NotNull [] readAllBytes(@NotNull Path path) {
        return Files.readAllBytes(path);
    }
}
