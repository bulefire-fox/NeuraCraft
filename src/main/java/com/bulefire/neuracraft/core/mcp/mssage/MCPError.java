package com.bulefire.neuracraft.core.mcp.mssage;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MCPError {
    private int code;
    private String message;
    private Object data;

    // 标准错误码
    /**
     * JSON解析错误
     */
    public static final int PARSE_ERROR = - 32700;
    /**
     * 无效的请求
     */
    public static final int INVALID_REQUEST = - 32600;
    /**
     * 方法未找到
     */
    public static final int METHOD_NOT_FOUND = - 32601;
    /**
     * 无效的参数
     */
    public static final int INVALID_PARAMS = - 32602;
    /**
     * 内部错误
     */
    public static final int INTERNAL_ERROR = - 32603;
    /// -32000 to -32099	Server error	服务器错误（保留）
    
    public MCPError(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    @Contract(pure = true)
    public static @NotNull String getCodeString(int code){
        return switch (code) {
            case PARSE_ERROR -> "Parse error";
            case INVALID_REQUEST -> "Invalid request";
            case METHOD_NOT_FOUND -> "Method not found";
            case INVALID_PARAMS -> "Invalid params";
            case INTERNAL_ERROR -> "Internal error";
            default -> "Unknown error";
        };
    }
}
