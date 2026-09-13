package com.bulefire.neuracraft.compatibility.util;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilTest {
    @Test
    void writesJsonAtomicallyAndCanReadItBack() throws Exception {
        Path directory = Files.createTempDirectory("neuracraft-fileutil");
        Path path = directory.resolve("config.json");
        Payload expected = new Payload("value", 7);

        AtomicJsonFileWriter.write(expected, path);

        var json = com.google.gson.JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        assertEquals(expected.name(), json.get("name").getAsString());
        assertEquals(expected.count(), json.get("count").getAsInt());
    }

    public static record Payload(String name, int count) {
    }
}
