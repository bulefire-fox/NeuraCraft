package com.bulefire.neuracraft.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
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

}
