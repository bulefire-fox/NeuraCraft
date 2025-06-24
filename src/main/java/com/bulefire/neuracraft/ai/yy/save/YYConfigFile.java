package com.bulefire.neuracraft.ai.yy.save;

import com.bulefire.neuracraft.ai.AIConfigFile;
import com.bulefire.neuracraft.ai.AIModels;

import java.util.List;

public class YYConfigFile extends AIConfigFile {
    private String chatId;

    public YYConfigFile() {
        super();
    }

    public YYConfigFile(String name, List<String> playerList, AIModels model, List<String> adminList, String disPlayName, String chatId) {
        super(name,playerList, model, adminList,disPlayName);
        this.chatId = chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }
}
