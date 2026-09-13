package com.bulefire.neuracraft.compatibility;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.InitCore;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class InitC {
    public static void init() {
        FileUtil.init();
        // 执行完兼容层的初始化后在执行核心层的初始化
        InitCore.init();
    }
    
    public static void afterInit() {
        FileUtil.afterInit();
        InitCore.afterInit();
    }
}
