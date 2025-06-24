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
     * 管理员列表
     */
    private List<String> adminList;
    /**
     * 模型
     */
    private AIModels model;
    /**
     * 显示名
     */
    private String disPlayName;

    public AIConfigFile(){

    }

    public AIConfigFile(String name, List<String> playerList, AIModels model, List<String> adminList,String disPlayName){
        this.name = name;
        this.playerList = playerList;
        this.model = model;
        this.adminList = adminList;
        this.disPlayName = disPlayName;
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

    public List<String> getAdminList() {
        return adminList;
    }

    public void setAdminList(List<String> adminList) {
        this.adminList = adminList;
    }

    public String getDisPlayName() {
        return disPlayName;
    }

    public void setDisPlayName(String disPlayName) {
        this.disPlayName = disPlayName;
    }
}
