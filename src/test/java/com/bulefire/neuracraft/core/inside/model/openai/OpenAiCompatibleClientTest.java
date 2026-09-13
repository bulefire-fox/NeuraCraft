package com.bulefire.neuracraft.core.inside.model.openai;

import com.bulefire.neuracraft.core.inside.model.ChatRequest;
import com.bulefire.neuracraft.core.inside.model.ModelException;
import com.bulefire.neuracraft.core.inside.model.deepseek.ChatHistory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenAiCompatibleClientTest {
    @Test
    void parsesStandardOpenAiResponse() {
        var response = OpenAiCompatibleClient.parseResponse("""
                {"choices":[{"message":{"role":"assistant","content":"hello"}}]}
                """);

        assertEquals("assistant", response.role());
        assertEquals("hello", response.content());
    }

    @Test
    void parsesStandardOpenAiToolCalls() {
        var response = OpenAiCompatibleClient.parseResponse("""
                {
                  "choices": [{
                    "message": {
                      "role": "assistant",
                      "content": null,
                      "tool_calls": [{
                        "id": "call_123",
                        "type": "function",
                        "function": {
                          "name": "tool_game_query_time",
                          "arguments": "{\\"level\\":\\"overworld\\"}"
                        }
                      }]
                    }
                  }]
                }
                """);

        assertTrue(response.hasToolCalls());
        assertEquals("call_123", response.toolCalls().get(0).id());
        assertEquals("tool_game_query_time", response.toolCalls().get(0).functionName());
        assertEquals("{\"level\":\"overworld\"}", response.toolCalls().get(0).arguments());
    }

    @Test
    void normalResponseHasNoToolCalls() {
        var response = OpenAiCompatibleClient.parseResponse(
                "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"hello\"}}]}"
        );

        assertFalse(response.hasToolCalls());
    }

    @Test
    void serializesNativeToolCallingRequest() {
        var toolCall = new OpenAiToolCall(
                "call_123",
                "function",
                new OpenAiToolCall.Function("tool_game_query_time", "{\"level\":\"overworld\"}")
        );
        var request = new ChatRequest(
                "http://localhost",
                "",
                "test-model",
                List.of(
                        new ChatHistory.ChatBlock("assistant", null, List.of(toolCall), null),
                        ChatHistory.ChatBlock.tool("call_123", "当前时间: 00:47:06")
                ),
                List.of(new OpenAiTool(
                        "function",
                        new OpenAiTool.Function(
                                "tool_game_query_time",
                                "查询时间",
                                Map.of("type", "object")
                        )
                ))
        );

        String json = OpenAiCompatibleClient.serializeRequest(request);

        assertTrue(json.contains("\"tools\""));
        assertTrue(json.contains("\"tool_calls\""));
        assertTrue(json.contains("\"role\":\"tool\""));
        assertTrue(json.contains("\"tool_call_id\":\"call_123\""));
        assertEquals(-1, json.indexOf("\"tool_call\":{\""));
    }

    @Test
    void parsesJsonObjectWrappedInAString() {
        var response = OpenAiCompatibleClient.parseResponse("""
                "{\\"choices\\":[{\\"message\\":{\\"role\\":\\"assistant\\",\\"content\\":\\"hello\\"}}]}"
                """);

        assertEquals("hello", response.content());
    }

    @Test
    void rejectsJsonStringInsteadOfOpenAiObject() {
        var exception = assertThrows(ModelException.class,
                () -> OpenAiCompatibleClient.parseResponse("\"hello\""));

        assertEquals(ModelException.Kind.JSON, exception.getKind());
    }

    @Test
    void rejectsPlainTextResponse() {
        var exception = assertThrows(ModelException.class,
                () -> OpenAiCompatibleClient.parseResponse("upstream gateway failure"));

        assertEquals(ModelException.Kind.JSON, exception.getKind());
    }

    @Test
    void rejectsHtmlResponseWithEndpointError() {
        var exception = assertThrows(ModelException.class,
                () -> OpenAiCompatibleClient.parseResponse("<!doctype html><html><body>Bad Gateway</body></html>"));

        assertEquals(ModelException.Kind.HTTP, exception.getKind());
    }

    @Test
    void parsesLenientObjectResponse() {
        var response = OpenAiCompatibleClient.parseResponse("""
                {'choices':[{'message':{'role':'assistant','content':'hello'},}],}
                """);

        assertEquals("hello", response.content());
    }

    @Test
    void parsesJsonInsideMarkdownCodeFence() {
        var response = OpenAiCompatibleClient.parseResponse("""
                ```json
                {"choices":[{"message":{"role":"assistant","content":"hello"}}]}
                ```
                """);

        assertEquals("hello", response.content());
    }

    @Test
    void rejectsEmptyResponse() {
        assertThrows(RuntimeException.class, () -> OpenAiCompatibleClient.parseResponse(" "));
    }
}
