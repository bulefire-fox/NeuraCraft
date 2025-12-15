package com.bulefire.neuracraft.core;

import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.config.NCMainConfig;

public class InitCore {
    public static void init(){
        // TODO:加载第三方Agent插件

        // 配置文件初始化
        NCMainConfig.init();
        // controller 初始化
        AgentController.init();
    }
}
