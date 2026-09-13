package com.bulefire.neuracraft.core.inside.model.deepseek;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatHistoryTest {
    @Test
    void serializesToolResultAsToolMessage() {
        String json = new Gson().toJson(
                ChatHistory.ChatBlock.tool("call_123", "overworld 当前时间: 00:47:06")
        );

        assertTrue(json.contains("\"role\":\"tool\""));
        assertTrue(json.contains("\"tool_call_id\":\"call_123\""));
        assertTrue(json.contains("\"content\":\"overworld 当前时间: 00:47:06\""));
        assertEquals(-1, json.indexOf("\"role\":\"user\""));
    }

    @Test
    void deserializesLegacyContentArray() {
        ChatHistory history = new Gson().fromJson(
                "{\"histories\":[{\"role\":\"system\",\"content\":[{\"type\":\"text\",\"text\":\"旧提示词\"}]}]}",
                ChatHistory.class
        );

        assertEquals("system", history.getHistories().get(0).role());
        assertTrue(history.getHistories().get(0).content() instanceof List<?>);
    }
}
