package com.bulefire.neuracraft.compatibility.function.process;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Log4j2
public class LevelLoadEventProcess {
    private static final List<Runnable> messageFun = new ArrayList<>();
    
    public static void registerFun(Runnable fun) {
        log.debug("register fun {} to chatEvent", fun);
        messageFun.add(fun);
    }
    
    public static void onLeveLoad() {
        log.debug("enter in onLeveLoad");
        log.debug(messageFun);
        for (Runnable fun : messageFun) {
            fun.run();
        }
    }
}
