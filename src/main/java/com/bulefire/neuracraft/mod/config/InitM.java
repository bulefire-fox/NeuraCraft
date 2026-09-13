package com.bulefire.neuracraft.mod.config;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.InitC;
import com.bulefire.neuracraft.mod.util.MUtil;

public class InitM {
    public static void init() {
        MUtil.init();
        // 执行完mod层的初始化后,再初始化兼容层
        InitC.init();
    }
    
    public static void afterInit() {
        MUtil.afterInit();
        InitC.afterInit();
    }
}
