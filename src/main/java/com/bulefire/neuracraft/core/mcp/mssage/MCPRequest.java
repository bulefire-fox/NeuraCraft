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
    private String id;
    private String name;
    private Map<String, Object> params;
}
