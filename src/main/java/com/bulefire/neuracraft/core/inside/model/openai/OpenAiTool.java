package com.bulefire.neuracraft.core.inside.model.openai;

import java.util.Map;

/**
 * Standard OpenAI function tool definition.
 */
public record OpenAiTool(String type, Function function) {
    public record Function(String name, String description, Map<String, Object> parameters) {
    }
}
