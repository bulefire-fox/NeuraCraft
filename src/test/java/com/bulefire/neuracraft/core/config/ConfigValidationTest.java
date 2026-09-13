package com.bulefire.neuracraft.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigValidationTest {
    @Test
    void acceptsValidModelConfig() {
        assertDoesNotThrow(() -> ConfigValidation.validateModel(
                "model", "Display", 1, "https://example.com/api"
        ));
    }

    @Test
    void rejectsInvalidModelConfig() {
        assertThrows(IllegalArgumentException.class, () ->
                ConfigValidation.validateModel("", "Display", 1, "https://example.com"));
        assertThrows(IllegalArgumentException.class, () ->
                ConfigValidation.validateModel("model", "Display", 0, "https://example.com"));
        assertThrows(IllegalArgumentException.class, () ->
                ConfigValidation.validateModel("model", "Display", 1, "file:///tmp/model"));
    }
}
