package com.bulefire.neuracraft.core.mcp.extern.fuckforge;

import com.bulefire.neuracraft.compatibility.util.CUtil;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;

@Log4j2
public class FuckCwpClassLoader extends URLClassLoader {
    public FuckCwpClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        log.warn("init FuckCwpClassLoader in thread {}", Thread.currentThread().getName());
        try {
            String build = System.getProperty("neuracraft.development.classloader.url.frombuild");
            URL modJarUrl;
            if (build != null && build.equals("true")) {
                String modJarPath = Objects.requireNonNull(System.getProperty("neuracraft.development.classloader.url"));
                log.warn("development mode with url {}", modJarPath);
                // jar URL 格式: jar:file:///path/to/file.jar!/  (Windows 路径需将 \ 转为 /)
                String normalizedPath = Path.of(modJarPath).toAbsolutePath().toString().replace('\\', '/');
                modJarUrl = new URL("jar:file:///" + normalizedPath + "!/");
            } else {
                modJarUrl = CUtil.getModJarPath.get().toUri().toURL();
                log.warn("production mode with url {}", modJarUrl);
            }
            log.debug("mod jar url: {}", modJarUrl);
            addURL(modJarUrl);
        } catch (Exception e) {
            throw new RuntimeException("MCP classloader init failed "+e);
        }
    }
    
    @Override
    protected Class<?> loadClass(@NotNull String name, boolean resolve) throws ClassNotFoundException {
        // 对MCP相关类使用这个类加载器，其他类委托给父类
        if (name.startsWith("io.modelcontextprotocol") || name.startsWith("io/modelcontextprotocol")) {
            log.debug("try load MCP class {} in thread {}", name, Thread.currentThread().getName());
            // 首先检查是否已加载
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    log.debug("try load MCP class {} from this classloader", name);
                    try {
                        c = findClass(name);
                        log.debug("load MCP class {} success {}", name, c);
                    } catch (ClassNotFoundException e) {
                        // 如果找不到，尝试父类加载器
                        log.debug("try load MCP class {} from parent classloader", name);
                        c = super.loadClass(name, resolve);
                    }
                }
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
        // 其他类使用父类加载器
        log.debug("try load class {} from parent classloader in thread {}", name, Thread.currentThread().getName());
        return super.loadClass(name, resolve);
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        log.debug("close MCP classloader");
    }
}
