package com.bulefire.neuracraft.core.mcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToolIdTest {
    @Test
    void toolIdsUseValueEquality() {
        assertEquals(new ToolId("server.tool"), new ToolId("server.tool"));
        assertEquals("server.tool", new ToolId("server.tool").toString());
    }

    @Test
    void blankToolIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ToolId(" "));
    }
}
