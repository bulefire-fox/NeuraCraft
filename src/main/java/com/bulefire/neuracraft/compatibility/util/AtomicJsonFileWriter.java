package com.bulefire.neuracraft.compatibility.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class AtomicJsonFileWriter {
    private AtomicJsonFileWriter() {
    }

    public static void write(@NotNull Object data, @NotNull Path filePath) throws IOException {
        Path parent = filePath.toAbsolutePath().getParent();
        if (parent != null)
            Files.createDirectories(parent);
        Gson gson = new GsonBuilder()
                .disableInnerClassSerialization()
                .setPrettyPrinting()
                .create();
        Path temp = Files.createTempFile(parent, filePath.getFileName().toString(), ".tmp");
        try {
            try (FileWriter writer = new FileWriter(temp.toFile())) {
                gson.toJson(data, writer);
                writer.flush();
            }
            try {
                Files.move(temp, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (JsonIOException e) {
            Files.deleteIfExists(temp);
            throw new IOException("Failed to serialize JSON to " + filePath, e);
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(temp);
            throw e;
        }
    }
}
