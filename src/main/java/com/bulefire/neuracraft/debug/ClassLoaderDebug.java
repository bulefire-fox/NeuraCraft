package com.bulefire.neuracraft.debug;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

@Log4j2
public class ClassLoaderDebug {
    public static void debugClassLoading(@NotNull List<String> names) {
        log.warn("=== 类加载器调试信息 ===");
        log.warn("thread name {}", Thread.currentThread().getName());
        
        // 获取当前线程的类加载器
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        log.warn("当前线程上下文类加载器: {}", cl);
        printClassLoaderHierarchy(cl, 0);
        
        // 获取系统类加载器
        log.warn("\n系统类加载器: {}", ClassLoader.getSystemClassLoader());
        
        // 获取这个类自己的类加载器
        log.warn("ClassLoaderDebug类的类加载器: {}", ClassLoaderDebug.class.getClassLoader());
        
        // 尝试加载MCP类
        for (String name : names) {
            try {
                Class<?> mcpClass = cl.loadClass(name);
                log.warn("✅ 成功通过类加载器加载 {}: {}", name, mcpClass);
            } catch (ClassNotFoundException e) {
                log.warn("❌ 无法加载 {}: {}", name, e.getMessage());
                
                // 如果是URLClassLoader，打印类路径
                if (cl instanceof URLClassLoader) {
                    URL[] urls = ((URLClassLoader) cl).getURLs();
                    log.warn("\n类路径URLs:");
                    for (URL url : urls) {
                        log.warn("  {}", url.getFile());
                    }
                }
            }
        }
    }
    
    private static void printClassLoaderHierarchy(ClassLoader cl, int depth) {
        if (cl == null) return;
        
        String indent = "  ".repeat(depth);
        log.warn("{}└─ {}", indent, cl.getClass().getName());
        
        ClassLoader parent = cl.getParent();
        if (parent != null) {
            printClassLoaderHierarchy(parent, depth + 1);
        }
    }
}
