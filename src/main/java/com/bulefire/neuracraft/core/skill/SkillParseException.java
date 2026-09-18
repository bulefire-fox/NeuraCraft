package com.bulefire.neuracraft.core.skill;

public class SkillParseException extends IllegalArgumentException {
    public SkillParseException(String message) {
        super(message);
    }

    public SkillParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
