package com.bulefire.neuracraft.core.plugin;

import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.agent.annotation.Agent;
import com.bulefire.neuracraft.core.agent.annotation.RegisterAgent;
import com.bulefire.neuracraft.core.mcp.annotation.MCP;
import com.bulefire.neuracraft.core.mcp.annotation.RegisterMCP;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class PluginLoader {
    public static final List<Class<? extends Annotation>> PLUGIN_ANNOTATIONS = List.of(Agent.class, RegisterAgent.class, MCP.class, RegisterMCP.class);

    private static final PluginLoader INSTANCE = new PluginLoader();

    private final List<PluginContainer> plugins;

    private PluginLoader() {
        this.plugins = new ArrayList<>();
    }

    public static PluginLoader getInstance() {
        return INSTANCE;
    }

    @SneakyThrows
    public void loadPluginsAndInvokeMethods() throws IOException {
        load(FileUtil.plugin_url);
    }

    @SneakyThrows
    public void loadSubModsAndInvokeMethods() throws IOException {
        load(FileUtil.mod_url);
    }

    private void load(Path path) throws IOException {
        log.info("Loading plugins");
        // load plugins
        this.plugins.addAll(PluginScanner.scanPlugins(path).stream()
                                         .map(PluginFile::new)
                                         .distinct()
                                         .map(PluginContainer::new)
                                         .peek(PluginContainer::load)
                                         .toList());

        // invoke methods
        for (PluginContainer plugin : this.plugins) {
            for (Class<? extends Annotation> annotation : PLUGIN_ANNOTATIONS) {
                if (annotation.equals(Agent.class) || annotation.equals(MCP.class)) {
                    plugin.getRegisteredData(annotation).getClasses().forEach(clazz -> {
                        try {
                            Method init;
                            try {
                                init = clazz.getMethod("init");
                            } catch (NoSuchMethodException e) {
                                log.info("Class {} does not have init method, wish it has static${$}", clazz.getName());
                                return;
                            }
                            init.invoke(null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else if (annotation.equals(RegisterAgent.class) || annotation.equals(RegisterMCP.class)) {
                    plugin.getRegisteredData(annotation).getMethods().forEach(method -> {
                        try {
                            log.info("invoke {} class {} method, in null", method.getDeclaringClass().getName(), method.getName());
                            method.setAccessible(true);
                            method.invoke(null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    throw new RuntimeException("Annotation " + annotation.getName() + " is not supported");
                }
            }
        }
    }
}
