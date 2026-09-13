package com.bulefire.neuracraft.core.inside.model.deepseek;

import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.core.inside.model.ChatResponse;
import com.bulefire.neuracraft.core.inside.model.openai.OpenAiToolCall;
import com.google.gson.annotations.SerializedName;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@ToString
public class ChatHistory {
    private static final Logger log = LoggerFactory.getLogger(ChatHistory.class);
    List<ChatBlock> histories;

    public ChatHistory() {
        this.histories = new ArrayList<>(16);
    }

    public ChatHistory(@NotNull List<ChatBlock> histories) {
        this.histories = histories;
    }

    public void addBlock(@NotNull ChatBlock b) {
        this.histories.add(b);
    }

    public @NotNull List<ChatBlock> getHistories() {
        return histories;
    }

    public @Nullable ChatBlock getSystemBlock() {
        for (ChatBlock b : histories) {
            if (b.role.equals("system")) {
                return b;
            }
        }
        return null;
    }

    public void replaceSystemContent(@NotNull Object content) {
        for (int i = 0; i < histories.size(); i++) {
            ChatBlock block = histories.get(i);
            if ("system".equals(block.role())) {
                histories.set(i, new ChatBlock("system", content));
                return;
            }
        }
        histories.add(0, new ChatBlock("system", content));
    }
    
    public static class ChatBlock {
        private String role;
        @SerializedName("content")
        private Object content;
        @SerializedName("tool_calls")
        private List<OpenAiToolCall> toolCalls;
        @SerializedName("tool_call_id")
        private String toolCallId;

        public ChatBlock() {
        }

        public ChatBlock(String role, Object content) {
            this(role, content, List.of(), null);
        }

        public ChatBlock(
                String role,
                Object content,
                List<OpenAiToolCall> toolCalls,
                String toolCallId
        ) {
            this.role = role;
            this.content = content;
            this.toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
            this.toolCallId = toolCallId;
        }

        public static ChatBlock assistant(@NotNull ChatResponse response) {
            return new ChatBlock("assistant", response.content(), response.toolCalls(), null);
        }

        public static ChatBlock tool(@NotNull String toolCallId, @NotNull String content) {
            return new ChatBlock("tool", content, List.of(), toolCallId);
        }

        public List<OpenAiToolCall> toolCallsOrEmpty() {
            return toolCalls == null ? List.of() : toolCalls;
        }

        public String role() {
            return role;
        }

        public Object content() {
            return content;
        }

        public List<OpenAiToolCall> toolCalls() {
            return toolCalls;
        }

        public String toolCallId() {
            return toolCallId;
        }
    }
}
