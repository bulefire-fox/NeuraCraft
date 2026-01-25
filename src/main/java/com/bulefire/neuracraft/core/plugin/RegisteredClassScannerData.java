package com.bulefire.neuracraft.core.plugin;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RegisteredClassScannerData {
    @Getter
    private final Class<? extends Annotation> scanAnnotation;
    @Getter
    private final List<Class<?>> classes;
    @Getter
    private final List<Method> methods;

    public RegisteredClassScannerData(Class<? extends Annotation> scanAnnotation) {
        this.scanAnnotation = scanAnnotation;
        this.classes = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    public void addClass(Class<?> clazz) {
        this.classes.add(clazz);
    }

    public void addClasses(List<Class<?>> classes) {
        this.classes.addAll(classes);
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public void addMethods(List<Method> methods) {
        this.methods.addAll(methods);
    }
}
