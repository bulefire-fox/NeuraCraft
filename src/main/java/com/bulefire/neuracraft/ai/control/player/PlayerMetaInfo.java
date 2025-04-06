package com.bulefire.neuracraft.ai.control.player;

public class PlayerMetaInfo {
    private String ChatName;

    public String getChatName() {
        return ChatName;
    }

    public void setChatName(String chatName) {
        ChatName = chatName;
    }

    public PlayerMetaInfo() {
    }

    public PlayerMetaInfo(String chatName) {
        ChatName = chatName;
    }
}
