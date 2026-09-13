package com.bulefire.neuracraft.core.plugin;

import com.bulefire.neuracraft.compatibility.function.process.ModLoadEventProcesser;

import java.io.IOException;

public class SubModLinker {
    public static void init() {
        // 注册子mod加载器， 在所有模组加载后执行
        ModLoadEventProcesser.registerFun(
                () -> {
                    try {
                        PluginLoader.getInstance().loadSubModsAndInvokeMethods();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
