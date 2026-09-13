package com.bulefire.neuracraft.core.inside.model;

import com.bulefire.neuracraft.core.inside.model.deepseek.ChatHistory;
import com.bulefire.neuracraft.core.inside.model.openai.OpenAiTool;

import java.util.List;

public record ChatRequest(
        String url,
        String token,
        String model,
        List<ChatHistory.ChatBlock> messages,
        List<OpenAiTool> tools
) {
    public ChatRequest(String url, String token, String model, List<ChatHistory.ChatBlock> messages) {
        this(url, token, model, messages, List.of());
    }

    public ChatRequest {
        tools = tools == null ? List.of() : List.copyOf(tools);
    }
}
