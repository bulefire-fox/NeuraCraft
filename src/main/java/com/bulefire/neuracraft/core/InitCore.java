package com.bulefire.neuracraft.core;

import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.config.ConfigFileWatcher;
import com.bulefire.neuracraft.core.config.NCMainConfig;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.plugin.PluginLoader;
import com.bulefire.neuracraft.core.plugin.SubModLinker;

import java.io.IOException;

public class InitCore {
    public static void init() {
        // 加载第三方Agent插件
        try {
            PluginLoader.getInstance().loadPluginsAndInvokeMethods();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 配置子mod处理器
        SubModLinker.init();
        // 配置文件初始化
        NCMainConfig.init();
        // mcp 初始化
        MCPController.getInstance().init();
        // controller 初始化
        AgentController.init();
        // 启动配置文件热重载监视器 (daemon线程, 随JVM退出)
        ConfigFileWatcher.start();
    }

    public static void afterInit() {
        MCPController.getInstance().startExternal();
        AgentController.afterInit();
    }
}
