package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPMessageImmutabilityTest {
    @Test
    void requestCopiesParameters() {
        var params = new HashMap<String, Object>();
        params.put("value", "before");
        var request = MCPRequest.builder().name("tool").params(params).build();

        params.put("value", "after");

        assertEquals("before", request.getParams().get("value"));
        assertThrows(UnsupportedOperationException.class,
                () -> request.getParams().put("other", "value"));
    }

    @Test
    void responseResultIsImmutableAndPrefixDoesNotMutateOriginal() {
        var original = new MCPResponse.Result(List.of(Content.of("text", "ok")), false);
        var updated = original.appendTextFirst("[成功] ");

        assertEquals("ok", original.getContent().get(0).textOrThrow());
        assertEquals("[成功] ok", updated.getContent().get(0).textOrThrow());
        assertThrows(UnsupportedOperationException.class,
                () -> original.getContent().add(Content.of("text", "other")));
    }

    @Test
    void messageJsonKeepsTheMcpErrorFieldName() {
        var response = MCPResponse.builder()
                .id("1")
                .result(new MCPResponse.Result(List.of(Content.of("text", "failed")), true))
                .build();

        String json = MCPMessage.toJson(response);

        assertEquals(true, new com.google.gson.Gson().fromJson(json, com.google.gson.JsonObject.class)
                .getAsJsonObject("result").get("isError").getAsBoolean());
    }
}
