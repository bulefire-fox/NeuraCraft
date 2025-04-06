package com.bulefire.neuracraft.ai.control;

import com.bulefire.neuracraft.config.yy.BaseInformation;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Times {
    private static final Logger log = LogUtils.getLogger();

    private static final int timesPerMin = BaseInformation.times;
    private static int times = 0;

    private static final ScheduledExecutorService t = Executors.newScheduledThreadPool(1);

    public static void add() {
        log.info("add times");
        times++;
    }

    public static void reset() {
        log.info("reset times");
        times = 0;
    }

    public static boolean isTimes() {
        return times >= timesPerMin;
    }

    public static void init(){
        t.scheduleAtFixedRate(Times::reset,0,1, TimeUnit.MINUTES);
    }
}
