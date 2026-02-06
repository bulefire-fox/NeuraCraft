package com.bulefire.neuracraft.core.mcp.mssage;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public non-sealed class MCPNotification implements MCPMessage {
    private String jsonrpc;
    private String method;
    private Map<String, Object> params;
}
