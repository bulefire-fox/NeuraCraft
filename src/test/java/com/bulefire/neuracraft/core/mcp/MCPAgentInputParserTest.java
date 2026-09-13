package com.bulefire.neuracraft.core.mcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPAgentInputParserTest {
    @Test
    void parsesAndAssignsRequestId() {
        var request = MCPAgentInputParser.parse("""
                {"tool_call":{"id":"weather","parameters":{"level":"overworld"}}}
                """);

        assertNotNull(request.getId());
        assertEquals("weather", request.getToolId().value());
        assertEquals("overworld", request.getParams().get("level"));
    }

    @Test
    void rejectsMalformedToolCallEnvelope() {
        assertThrows(IllegalArgumentException.class,
                () -> MCPAgentInputParser.parse("{\"tool_call\":{\"id\":\"weather\"}}"));
    }
}
