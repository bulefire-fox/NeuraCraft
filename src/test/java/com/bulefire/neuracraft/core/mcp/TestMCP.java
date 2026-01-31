package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.core.mcp.mssage.MCPError;
import com.bulefire.neuracraft.core.mcp.mssage.MCPMessage;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.bulefire.neuracraft.core.mcp.mssage.MCPResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

public class TestMCP {
    MCPController controller;
    MCPManager manager;
    
    @BeforeEach
    public void init() {
        controller = MCPController.getInstance();
        manager = controller.getMcpManager();
    }
    
    @Test
    @Order(1)
    public void testRegisterTool() {
        var success = new AbsMCPTool("tool/success", "it will success", new MCPToolInfo()) {
            @Override
            public @NotNull MCPResponse execute(@NotNull MCPRequest request) {
                return MCPMessage.responseSuccessBuilder()
                                 .id("success")
                                 .result("success")
                                 .build();
            }
        };
        var failed = new AbsMCPTool("tool/failed","it will failed", new MCPToolInfo()) {
            @Override
            public @NotNull MCPResponse execute(@NotNull MCPRequest request) {
                return MCPMessage.responseFailedBuilder()
                                 .id("failed")
                                 .error(new MCPError(MCPError.INVALID_PARAMS, "invalid params", null))
                                 .build();
            }
        };
        
        manager.registerTool(success);
        manager.registerTool(failed);
    }
    
    @Test
    @Order(2)
    public void test() {
        var successResponse = controller.callTool(MCPMessage.requestBuilder()
                                      .method("tool/success")
                                      .id("success")
                                      .build());
        System.out.println(successResponse);
        var failedResponse = controller.callTool(MCPMessage.requestBuilder()
                                      .method("tool/failed")
                                      .id("failed")
                                      .build());
        System.err.println(failedResponse);
    }
}
