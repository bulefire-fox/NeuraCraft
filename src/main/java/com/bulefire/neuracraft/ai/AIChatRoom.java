package com.bulefire.neuracraft.ai;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
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

    @Override
    public abstract @NotNull String sendMessage(@NotNull String message);
    @Override
    public abstract void save() throws IOException;
    @Override
    public abstract void load(@NotNull Path path) throws IOException;
    @Override
    public abstract void delete() throws IOException;
}
