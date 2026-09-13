package com.bulefire.neuracraft.core.mcp.entity;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentInputTest {
    @Test
    void parsesToolCallPayload() {
        String json = """
                {
                  "tool_call": {
                    "id": "weather",
                    "parameters": {
                      "location": "Perth"
                    }
                  }
                }
                """;

        AgentInput input = new Gson().fromJson(json, AgentInput.class);

        assertEquals("weather", input.getTool_call().getId());
        assertEquals("Perth", input.getTool_call().getParameters().get("location"));
    }
}
