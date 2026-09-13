package com.bulefire.neuracraft.compatibility.function.process;

import java.util.HashSet;
import java.util.Set;

public class ServerStoppingEventProcesser {
    private static final Set<Runnable> messageFunctions = new HashSet<>();

    public static void registerFun(Runnable fun) {
        messageFunctions.add(fun);
    }

    public static void onServerStopping() {
        for (Runnable fun : messageFunctions) {
            fun.run();
        }
    }
}
