package com.bulefire.neuracraft.ai;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AIChatRoom implements AI {
    /**
     * 聊天室名称
     */
    public String name;
    /**
     * 玩家列表
     */
    public List<String> playerList;
    /**
     * 管理员列表
     */
    public List<String> adminList;
    /**
     * 模型
     */
    public AIModels model;

    /**
     * 展示名
     */
    public String disPlayName;

    public AIChatRoom(@NotNull String name, @NotNull List<String> playerList, @NotNull AIModels model, @NotNull List<String> adminList,@NotNull String disPlayName) {
        this.name = name;
        this.playerList = playerList;
        this.model = model;
        this.adminList = adminList;
        this.disPlayName = disPlayName;
    }

    public AIChatRoom(@NotNull String name,@NotNull AIModels model, @NotNull String disPlayName) {
        this.name = name;
        playerList = new ArrayList<>();
        adminList = new ArrayList<>();
        this.model = model;
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

    @Override
    public abstract @NotNull String sendMessage(@NotNull String message);
    @Override
    public abstract void save() throws IOException;
    @Override
    public abstract void load(@NotNull Path path) throws IOException;
    @Override
    public abstract void delete() throws IOException;
}
