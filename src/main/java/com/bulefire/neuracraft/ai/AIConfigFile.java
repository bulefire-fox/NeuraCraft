package com.bulefire.neuracraft.ai;

import java.util.List;

public abstract class AIConfigFile {
    /**
     * 聊天室名称
     */
    private String name;
    /**
     * 玩家列表
     */
    private List<String> playerList;
    /**
     * 模型
     */
    private AIModels model;

    public AIConfigFile(){

    }

    public AIConfigFile(String name, List<String> playerList, AIModels model){
        this.name = name;
        this.playerList = playerList;
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<String> playerList) {
        this.playerList = playerList;
    }

    public AIModels getModel() {
        return model;
    }

    public void setModel(AIModels model) {
        this.model = model;
    }
}
