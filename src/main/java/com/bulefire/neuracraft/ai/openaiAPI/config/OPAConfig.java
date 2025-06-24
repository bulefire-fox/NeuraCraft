package com.bulefire.neuracraft.ai.openaiAPI.config;

import com.bulefire.neuracraft.ai.AIConfigFile;
import com.bulefire.neuracraft.ai.AIModels;
import com.bulefire.neuracraft.ai.openaiAPI.ChatHistory;

import java.util.List;

public class OPAConfig extends AIConfigFile {
    private String chatModel;
    private ChatHistory chatHistory;

    public OPAConfig() {
        super();
    }

    public OPAConfig(String name, List<String> playerList, AIModels model, List<String> adminList, String displayName, String modelName, ChatHistory chatHistory) {
        super(name,playerList,model,adminList,displayName);
        this.chatModel = modelName;
        this.chatHistory = chatHistory;
    }

    public String getChatModel() {
        return chatModel;
    }

    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    public void setChatHistory(ChatHistory chatHistory) {
        this.chatHistory = chatHistory;
    }
}
