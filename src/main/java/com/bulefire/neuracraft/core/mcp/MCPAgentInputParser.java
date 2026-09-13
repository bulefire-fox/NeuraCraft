package com.bulefire.neuracraft.core.mcp;

import com.bulefire.neuracraft.core.mcp.entity.AgentInput;
import com.bulefire.neuracraft.core.mcp.mssage.MCPRequest;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Parses the small tool-call envelope emitted by an Agent.
 *
 * <p>This class deliberately has no Minecraft dependency, so malformed model
 * output can be tested without starting a game.</p>
 */
public final class MCPAgentInputParser {
    private static final Gson GSON = new Gson();

    private MCPAgentInputParser() {}

    public static @NotNull MCPRequest parse(@NotNull String input) {
        if (input.isBlank()) {
            throw new IllegalArgumentException("输入为空");
        }

        final AgentInput agentInput;
        try {
            if (!JsonParser.parseString(input).isJsonObject()) {
                throw new IllegalArgumentException("JSON 根节点必须是对象");
            }
            agentInput = GSON.fromJson(input, AgentInput.class);
        } catch (JsonParseException | IllegalStateException e) {
            throw new IllegalArgumentException("JSON 格式错误", e);
        }

        if (agentInput == null || agentInput.getTool_call() == null) {
            throw new IllegalArgumentException("缺少 tool_call");
        }
        if (agentInput.getTool_call().getId() == null || agentInput.getTool_call().getId().isBlank()) {
            throw new IllegalArgumentException("缺少工具 ID");
        }
        if (agentInput.getTool_call().getParameters() == null) {
            throw new IllegalArgumentException("缺少 parameters");
        }

        return MCPRequest.builder()
                .id(UUID.randomUUID().toString())
                .name(agentInput.getTool_call().getId())
                .params(agentInput.getTool_call().getParameters())
                .build();
    }
}
