package com.bulefire.neuracraft.core.mcp.mssage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MCPResponseFailed {
    private String jsonrpc;
    private String id;
    private MCPError error;
}
