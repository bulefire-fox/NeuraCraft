package com.bulefire.neuracraft.ai;

import org.jetbrains.annotations.Nullable;

public enum AIModels {
    CyberFurry,
    OpenAI,
    Other;

    public static @Nullable AIModels getModel(String model) {
        for (AIModels m : AIModels.values()) {
            if (m.name().equals(model)) {
                return m;
            }
        }
        return null;
    }
}
