package com.bulefire.neuracraft.core.util;

import lombok.Getter;

public class UnSupportFormattedMessage extends RuntimeException {

    @Getter
    private final Type messageType;

    public UnSupportFormattedMessage(String message, Type messageType) {
        super(message);
        this.messageType = messageType;
    }

    public enum Type {
        JOIN,
        LEAVE,
        PLUGIN,
        UNKNOWN,
    }
}
