package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class MCPManagerTest {
    @Test
    void removingOneNameDoesNotRemoveOtherToolsWithSameDisplayName() {
        MCPManager manager = new MCPManager();
        MCPTool first = tool("one", "same");
        MCPTool second = tool("two", "same");
        manager.registerTool(first);
        manager.registerTool(second);

        assertSame(first, manager.removeToolByName("one"));
        assertNull(manager.getToolByName("one"));
        assertSame(second, manager.getToolByName("two"));
        assertEquals(1, manager.getToolByDisplayName("same").size());
        assertEquals(1, manager.getTools().size());
    }

    @Test
    void invalidToolIsRejected() {
        MCPManager manager = new MCPManager();
        MCPTool invalid = new AbsMCPTool("bad", "", MCPToolInfo.builder().build()) {
            @Override
            public MCPResponse execute(MCPRequest request, Consumer print) {
                return null;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> manager.registerTool(invalid));
    }

    @Test
    void duplicateNameReplacesOldToolWithoutLeavingStaleIndex() {
        MCPManager manager = new MCPManager();
        MCPTool first = tool("same-id", "first");
        MCPTool second = tool("same-id", "second");

        manager.registerTool(first);
        manager.registerTool(second);

        assertSame(second, manager.getToolByName("same-id"));
        assertNull(manager.getToolByDisplayName("first"));
        assertEquals(1, manager.getToolByDisplayName("second").size());
    }

    private static MCPTool tool(String name, String displayName) {
        return new AbsMCPTool(displayName, "description", MCPToolInfo.builder()
                .type(MCPToolInfo.Type.BUILT_IN)
                .name(name)
                .params(Map.of())
                .optional(Map.of())
                .build()) {
            @Override
            public MCPResponse execute(MCPRequest request, Consumer print) {
                return MCPResponse.builder()
                        .id(request.getId())
                        .result(new MCPResponse.Result(List.of(new Content("text", "ok")), false))
                        .build();
            }
        };
    }
}
