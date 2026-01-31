package com.bulefire.neuracraft.core.mcp;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class AbsMCPTool implements MCPTool {
    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final MCPToolInfo info;
    
    public AbsMCPTool(String name, String description, MCPToolInfo info) {
        this.name = name;
        this.description = description;
        this.info = info;
    }
    
    /*
    工具名称：
    - 唯一调用ID：[服务器名].[工具名]
    - 功能描述：[此工具具体能做什么]
    - 必需参数：[参数1名称] (数据类型，说明)，[参数2名称] (数据类型，说明)...
    - 可选参数：(如有) [参数3名称] (数据类型，默认值，说明)
     */
    @Override
    public @NotNull String getPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("工具名称:").append(name).append("\n\t")
          .append("- 唯一调用ID:").append(info.getMethod()).append("\n\t")
          .append("- 功能描述：").append(description).append("\n\t");
        if (! info.getParams().isEmpty())
            sb.append("- 必需参数：");
        for (Map.Entry<String, MCPToolInfo.Param> entry : info.getParams().entrySet()) {
            sb.append("[").append(entry.getKey()).append("] (")
              .append(entry.getValue().type()).append(", ").append(entry.getValue().describe()).append("), ");
        }
        if (! info.getOptional().isEmpty())
            sb.append("\n\t- 可选参数：");
        for (Map.Entry<String, MCPToolInfo.Param> entry : info.getOptional().entrySet()) {
            sb.append("[").append(entry.getKey()).append("] (")
              .append(entry.getValue().type()).append(", ").append(entry.getValue().describe()).append("), ");
        }
        return sb.toString();
    }
}
