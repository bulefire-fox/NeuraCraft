package com.bulefire.neuracraft.core.inside.model.deepseek;

import com.bulefire.neuracraft.compatibility.entity.Content;
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

    public @Nullable ChatBlock getSystemBlock() {
        for (ChatBlock b : histories) {
            if (b.role.equals("system")) {
                return b;
            }
        }
        return null;
    }
    
    public record ChatBlock(String role, List<Content> contents) {
    }
}
