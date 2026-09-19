package com.bulefire.neuracraft.core.inside.model.openai;

import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.core.inside.model.ChatRequest;
import com.bulefire.neuracraft.core.inside.model.ChatResponse;
import com.bulefire.neuracraft.core.inside.model.ModelClient;
import com.bulefire.neuracraft.core.inside.model.ModelException;
import com.bulefire.neuracraft.core.inside.model.deepseek.ChatHistory;
import com.bulefire.neuracraft.core.inside.model.deepseek.OPAResult;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.Locale;

public class OpenAiCompatibleClient implements ModelClient {
    private static final Gson GSON = new Gson();

    @Override
    public @NotNull ChatResponse send(@NotNull ChatRequest request) throws ModelException {
        String body = serializeRequest(request);
        final CUtil.Response response;
        try {
            response = CUtil.AiPOST(request.url(), body, request.token());
        } catch (Exception e) {
            throw new ModelException(ModelException.Kind.NETWORK, "Model request failed", e);
        }
        if (response.status() < 200 || response.status() >= 300)
            throw new ModelException(ModelException.Kind.HTTP, "Model returned HTTP " + response.status());
        try {
            return parseResponse(response.response());
        } catch (ModelException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ModelException(ModelException.Kind.JSON, "Unable to parse model response", e);
        }
    }

    static @NotNull String serializeRequest(@NotNull ChatRequest request) {
        return GSON.toJson(new SendBody(request.model(), request.messages(), request.tools()));
    }

    static @NotNull ChatResponse parseResponse(@NotNull String rawResponse) {
        if (rawResponse.isBlank()) {
            throw new ModelException(ModelException.Kind.JSON, "Model response is empty");
        }

        String normalized = stripCodeFence(rawResponse.trim());
        if (looksLikeHtml(normalized)) {
            throw new ModelException(
                    ModelException.Kind.HTTP,
                    "Model endpoint returned HTML instead of JSON: " + preview(normalized)
            );
        }
        JsonElement root;
        try {
            root = JsonParser.parseString(normalized);
        } catch (JsonParseException strictFailure) {
            try {
                JsonReader reader = new JsonReader(new StringReader(removeTrailingCommas(normalized)));
                reader.setStrictness(Strictness.LENIENT);
                root = JsonParser.parseReader(reader);
            } catch (JsonParseException | IllegalStateException lenientFailure) {
                if (looksLikeHtml(normalized)) {
                    throw new ModelException(
                            ModelException.Kind.HTTP,
                            "Model endpoint returned HTML instead of JSON: " + preview(normalized),
                            strictFailure
                    );
                }
                throw new ModelException(
                        ModelException.Kind.JSON,
                        "Model response is not valid JSON: " + preview(normalized),
                        strictFailure
                );
            }
        }

        if (root.isJsonPrimitive() && root.getAsJsonPrimitive().isString()) {
            String text = stripCodeFence(root.getAsString().trim());
            if (looksLikeHtml(text)) {
                throw new ModelException(
                        ModelException.Kind.HTTP,
                        "Model endpoint returned HTML instead of JSON: " + preview(text)
                );
            }
            if (text.startsWith("{") || text.startsWith("[")) {
                return parseResponse(text);
            }
            throw new ModelException(
                    ModelException.Kind.JSON,
                    "Model response must be a JSON object, got a JSON string: " + preview(text)
            );
        }
        if (!root.isJsonObject()) {
            throw new ModelException(ModelException.Kind.JSON, "Model response must be a JSON object");
        }

        OPAResult result = GSON.fromJson(root, OPAResult.class);
        if (result == null || result.getChoices() == null || result.getChoices().isEmpty()
                || result.getChoices().getFirst().getMessage() == null)
            throw new ModelException(ModelException.Kind.JSON, "Model response has no assistant message");
        var message = result.getChoices().getFirst().getMessage();
        var toolCalls = message.getToolCalls() == null ? java.util.List.<OpenAiToolCall>of() : message.getToolCalls();
        if (message.getContent() == null && toolCalls.isEmpty()) {
            throw new ModelException(ModelException.Kind.JSON, "Model response has no content or tool calls");
        }
        return new ChatResponse(
                message.getRole() == null ? "assistant" : message.getRole(),
                message.getContent(),
                toolCalls
        );
    }

    private static String stripCodeFence(String response) {
        if (response.startsWith("```") && response.endsWith("```")) {
            int firstLineEnd = response.indexOf('\n');
            if (firstLineEnd >= 0) {
                return response.substring(firstLineEnd + 1, response.length() - 3).trim();
            }
        }
        return response;
    }

    private static boolean looksLikeHtml(String response) {
        String lowerCase = response.stripLeading().toLowerCase(Locale.ROOT);
        return lowerCase.startsWith("<!doctype html")
                || lowerCase.startsWith("<html")
                || lowerCase.startsWith("<head")
                || lowerCase.startsWith("<body");
    }

    private static String preview(String response) {
        String singleLine = response.replaceAll("\\s+", " ").trim();
        return singleLine.length() <= 160 ? singleLine : singleLine.substring(0, 160) + "...";
    }

    private static String removeTrailingCommas(String response) {
        StringBuilder result = new StringBuilder(response.length());
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < response.length(); i++) {
            char current = response.charAt(i);
            if (current == '"' && !escaped) {
                inString = !inString;
            }
            if (!inString && current == ',') {
                int next = i + 1;
                while (next < response.length() && Character.isWhitespace(response.charAt(next))) {
                    next++;
                }
                if (next < response.length() && (response.charAt(next) == '}' || response.charAt(next) == ']')) {
                    continue;
                }
            }
            result.append(current);
            escaped = current == '\\' && !escaped;
        }
        return result.toString();
    }

    private record SendBody(
            String model,
            java.util.List<ChatHistory.ChatBlock> messages,
            java.util.List<OpenAiTool> tools
    ) {
    }
}
