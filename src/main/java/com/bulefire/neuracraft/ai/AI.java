package com.bulefire.neuracraft.ai;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public interface AI {
    @NotNull String sendMessage(@NotNull String message);
    void save() throws IOException;
    void load(Path path) throws IOException;
    void delete() throws IOException;
}
