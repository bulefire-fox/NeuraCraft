package com.bulefire.neuracraft.core.inside.model;

import com.bulefire.neuracraft.core.inside.model.openai.OpenAiToolCall;

import java.util.List;

public record ChatResponse(String role, String content, List<OpenAiToolCall> toolCalls) {
    public ChatResponse(String role, String content) {
        this(role, content, List.of());
    }

    public ChatResponse {
        toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
    }

    public boolean hasToolCalls() {
        return !toolCalls.isEmpty();
    }
}
