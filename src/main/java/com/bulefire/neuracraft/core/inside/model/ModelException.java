package com.bulefire.neuracraft.core.inside.model;

public class ModelException extends RuntimeException {
    public enum Kind {
        NETWORK, HTTP, JSON
    }

    private final Kind kind;

    public ModelException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public ModelException(Kind kind, String message) {
        this(kind, message, null);
    }

    public Kind getKind() {
        return kind;
    }
}
