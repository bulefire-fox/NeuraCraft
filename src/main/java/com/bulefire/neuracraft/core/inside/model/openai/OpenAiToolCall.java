package com.bulefire.neuracraft.core.inside.model.openai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Standard OpenAI assistant tool call.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiToolCall {
    private String id;
    private String type;
    private Function function;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Function {
        private String name;
        private String arguments;
    }

    public @NotNull String functionName() {
        if (function == null || function.getName() == null || function.getName().isBlank()) {
            throw new IllegalArgumentException("Tool call is missing function name");
        }
        return function.getName();
    }

    public @NotNull String arguments() {
        return function == null || function.getArguments() == null ? "{}" : function.getArguments();
    }

    public String id() {
        return id;
    }

    public String type() {
        return type;
    }

    public Function function() {
        return function;
    }
}
