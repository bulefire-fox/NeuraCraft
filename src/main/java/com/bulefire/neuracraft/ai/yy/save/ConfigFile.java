package com.bulefire.neuracraft.ai.yy.save;

import java.util.List;

public class ConfigFile {
    private List<ChatRoomBean> root;

    public static class ChatRoomBean {
        private String name;
        private String chatId;
        private List<String> playerList;

        public ChatRoomBean() {
        }

        public ChatRoomBean(String name, String chatId, List<String> playerList) {
            this.name = name;
            this.chatId = chatId;
            this.playerList = playerList;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getChatId() {
            return chatId;
        }

        public void setChatId(String chatId) {
            this.chatId = chatId;
        }

        public List<String> getPlayerList() {
            return playerList;
        }

        public void setPlayerList(List<String> playerList) {
            this.playerList = playerList;
        }
    }

    public List<ChatRoomBean> getRoot() {
        return root;
    }

    public void setRoot(List<ChatRoomBean> root) {
        this.root = root;
    }
}
