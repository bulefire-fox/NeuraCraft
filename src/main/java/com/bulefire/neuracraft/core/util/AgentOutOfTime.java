package com.bulefire.neuracraft.core.util;

public class AgentOutOfTime extends RuntimeException {
    private final int timePerMin;

    public AgentOutOfTime(String message) {
        super(message);
        this.timePerMin = - 1;
    }

    public AgentOutOfTime(String message, final int timePerMin) {
        super(message);
        this.timePerMin = timePerMin;
    }
}
