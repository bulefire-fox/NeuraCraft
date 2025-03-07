package com.bulefire.neuracraft.ai.yy;

import com.bulefire.neuracraft.config.yy.BaseInformation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Times {
    private static final int timesPerMin = BaseInformation.times;
    private static int times = 0;

    private static final ScheduledExecutorService t = Executors.newScheduledThreadPool(1);

    public static void add() {
        times++;
    }

    public static void reset() {
        times = 0;
    }

    public static boolean isTimes() {
        return times >= timesPerMin;
    }

    public static void init(){
        t.scheduleAtFixedRate(Times::reset,0,1, TimeUnit.MINUTES);
    }
}
