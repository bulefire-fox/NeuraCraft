package com.bulefire.neuracraft.core.inside.model.deepseek;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

    public record ChatBlock(String role, String content) {
    }

    @Override
    public String toString() {
        log.debug("Converting to String");
        StringBuilder sb = new StringBuilder();
        for (ChatBlock b : histories) {
            sb.append("{\"role\":\"").append(b.role).append("\",\"content\":\"").append(b.content).append("\"},");
        }
        String result = "[" + sb.substring(0, sb.length() - 1) + "]";
        log.debug("Converted to String: {}", result);
        return result;
    }
}
