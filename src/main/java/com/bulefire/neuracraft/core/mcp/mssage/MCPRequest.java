package com.bulefire.neuracraft.core.mcp.mssage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public non-sealed class MCPRequest implements MCPMessage {
    @Builder.Default
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private Map<String, Object> params;
}
