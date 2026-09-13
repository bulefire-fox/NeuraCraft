package com.bulefire.neuracraft.compatibility.entity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record APlayer(String name, UUID uuid) {
    @Contract(pure = true)
    public @NotNull String toFormatedString() {
        return "[" + name + "(" + uuid + ")] ";
    }
}
