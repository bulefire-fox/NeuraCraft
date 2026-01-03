package com.bulefire.neuracraft.compatibility.function.process;

import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Set;

@Log4j2
public class ModLoadEventProcesser {
    private static final Set<Runnable> messageFun = new HashSet<>();

    public static void registerFun(Runnable fun) {
        log.debug("register fun {} to chatEvent", fun);
        messageFun.add(fun);
    }

    public static void onLoadComplete(){
        for (Runnable fun : messageFun) {
            fun.run();
        }
    }
}
