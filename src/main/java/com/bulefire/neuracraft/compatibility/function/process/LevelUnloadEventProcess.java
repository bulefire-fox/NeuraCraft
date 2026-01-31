package com.bulefire.neuracraft.compatibility.function.process;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Log4j2
public class LevelUnloadEventProcess {
    private static final Set<Runnable> messageFun = new HashSet<>();
    
    public static void registerFun(Runnable fun) {
        log.debug("register fun {} to chatEvent", fun);
        messageFun.add(fun);
    }
    
    public static void onLeveUnload() {
        log.debug("enter in onLeveLoad");
        log.debug(messageFun);
        for (Runnable fun : messageFun) {
            fun.run();
        }
    }
}
