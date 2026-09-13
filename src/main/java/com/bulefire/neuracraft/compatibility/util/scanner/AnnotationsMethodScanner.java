package com.bulefire.neuracraft.compatibility.util.scanner;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Log4j2
public class AnnotationsMethodScanner {
    public static Path currentBasePath;

    static {
        try {
            URL location = AnnotationsMethodScanner.class.getProtectionDomain().getCodeSource().getLocation();
            // 使用 URI 来正确处理路径
            currentBasePath = Path.of(location.toURI());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize jar file path", e);
        }
    }

    public static @NotNull Set<Method> scanPackageToMethod(@NotNull final String packageName, @NotNull final Set<Class<? extends Annotation>> annotations) {
        Path packagePath = currentBasePath.resolve(packageName.replace(".", "/"));
        log.debug("currentBasePath: {}", currentBasePath);
        log.debug("package path: {}", packagePath);
        Set<Method> methods = new HashSet<>();
        try (var walk = Files.walk(packagePath)) {
            walk
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".class"))
                    .forEach(classPath -> {
                        //log.debug("file path: {}", classPath);
                        byte[] bytes;
                        try {
                            bytes = Files.readAllBytes(classPath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        var insideMethods = scannerClass(bytes, annotations, null);
                        //log.debug("insideMethods: {}", insideMethods);
                        methods.addAll(insideMethods);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return methods;
    }

    public static @NotNull Set<Method> scannerClass(final byte @NotNull [] bytes, @NotNull final Set<Class<? extends Annotation>> annotations, ClassLoader classLoader) {
        ClassReader reader = new ClassReader(bytes);
        Set<MethodInfo> methodDescriptor = new HashSet<>();
        Set<String> annClassDescriptor = new HashSet<>();
        for (Class<? extends Annotation> ann : annotations) {
            annClassDescriptor.add(Type.getDescriptor(ann));
        }
        ClassVisitor classVisitor = new PrepareClassVisitor(null, methodDescriptor, annClassDescriptor);
        Set<Method> methods = new HashSet<>();
        reader.accept(classVisitor, 2);
        for (MethodInfo methodInfo : methodDescriptor) {
            String className = methodInfo.className.replace('/', '.');
            try {
                Class<?> clazz;
                if (classLoader != null) {
                    clazz = classLoader.loadClass(className);
                } else {
                    clazz = Class.forName(className);
                }
                Type methodType = Type.getMethodType(methodInfo.descriptor());

                // 解析参数类型
                Type[] argumentTypes = methodType.getArgumentTypes();
                Class<?>[] parameterTypes = new Class<?>[argumentTypes.length];
                for (int i = 0; i < argumentTypes.length; i++) {
                    parameterTypes[i] = getClassFromType(argumentTypes[i]);
                }

                // 获取Method对象
                Method method = clazz.getDeclaredMethod(methodInfo.name(), parameterTypes);
                methods.add(method);
            } catch (Exception e) {
                log.error("Failed to create Method object", e);
            }
        }
        return methods;
    }

    private static Class<?> getClassFromType(@NotNull Type type) throws ClassNotFoundException {
        return switch (type.getSort()) {
            case Type.VOID -> void.class;
            case Type.BOOLEAN -> boolean.class;
            case Type.CHAR -> char.class;
            case Type.BYTE -> byte.class;
            case Type.SHORT -> short.class;
            case Type.INT -> int.class;
            case Type.FLOAT -> float.class;
            case Type.LONG -> long.class;
            case Type.DOUBLE -> double.class;
            case Type.OBJECT -> {
                String className = type.getClassName();
                yield Class.forName(className);
            }
            case Type.ARRAY -> {
                // 处理数组类型
                Class<?> componentClass = getClassFromType(type.getElementType());
                yield java.lang.reflect.Array.newInstance(componentClass, 0).getClass();
            }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    public static class PrepareClassVisitor extends ClassVisitor {
        private final Set<MethodInfo> methods;
        private final Set<String> annotations;
        private String className;

        protected PrepareClassVisitor(ClassVisitor classVisitor, Set<MethodInfo> s, Set<String> annotations) {
            super(Opcodes.ASM9, classVisitor);
            this.methods = s;
            this.annotations = annotations;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new PrepareClassVisitor.PrePareMethodVisitor(mv, access, name, descriptor, methods, annotations, className);
        }

        private static class PrePareMethodVisitor extends MethodVisitor {
            private final String name;
            private final String className;
            private final String methodDescriptor;
            private final Set<MethodInfo> methods;
            private final Set<String> annotations;

            public PrePareMethodVisitor(MethodVisitor mv, int access, String name, String descriptor, Set<MethodInfo> methods, Set<String> annotations, String className) {
                super(Opcodes.ASM9, mv);
                this.name = name;
                this.className = className;
                this.methodDescriptor = descriptor;
                this.methods = methods;
                this.annotations = annotations;
            }

            @Override
            public @Nullable AnnotationVisitor visitAnnotation(@NotNull String descriptor, boolean visible) {
                if (annotations.contains(descriptor)) {
                    methods.add(
                            new MethodInfo(
                                    className,
                                    name,
                                    methodDescriptor
                            )
                    );
                }
                return super.visitAnnotation(descriptor, visible);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
            }
        }
    }

    public record MethodInfo(String className, String name, String descriptor) {
    }
}
