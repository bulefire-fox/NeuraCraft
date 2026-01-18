package com.bulefire.neuracraft.core;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.util.AgentOutOfTime;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * 表示一个 Agent 的接口
 * 所有Agent都必须直接或间接的实现这个接口才能加入{@link AgentController}的管理
 *
 * @author bulefire_fox
 * @implNote 可以继承{@link AbsAgent}以获取默认骨干实现
 * @see AgentController
 * @see AgentMessage
 * @see APlayer
 * @see AbsAgent
 * @see AgentOutOfTime
 * @since 1.0
 */
public interface Agent {
    /**
     * 发送消息的方法
     *
     * @param message 传入的消息
     * @return 返回的消息
     * @throws AgentOutOfTime 当发送频率太快时抛出
     */
    @NotNull String sendMessage(@NotNull AgentMessage message) throws AgentOutOfTime;

    /**
     * 设置聊天室的名称，面向用户，可以重复
     *
     * @param name 聊天室的名称
     */
    void setName(@NotNull String name);

    /**
     * 获取聊天室的名称，面向用户，可以重复
     *
     * @return 聊天室的名称
     */
    @NotNull String getName();

    /**
     * 获取聊天室的唯一标识符UUID，面向程序，不能重复
     *
     * @return 聊天室的唯一标识符 UUID
     */
    @NotNull UUID getUUID();

    /**
     * 将玩家添加到聊天室中的玩家列表
     *
     * @param player 玩家
     */
    void addPlayer(@NotNull APlayer player);

    /**
     * 将玩家从聊天室中的玩家列表中移除
     *
     * @param player 玩家
     */
    void removePlayer(@NotNull APlayer player);

    /**
     * 获取聊天室中的玩家列表
     *
     * @return 聊天室中的玩家列表
     */
    @NotNull List<APlayer> getPlayers();

    /**
     * 将管理员添加到聊天室中的管理员列表
     *
     * @param player 管理员
     */
    void addAdmin(@NotNull APlayer player);

    /**
     * 判断玩家是否是管理员
     *
     * @param player 玩家
     * @return 是否是管理员
     */
    boolean hasAdmin(@NotNull APlayer player);

    /**
     * 将管理员从聊天室中的管理员列表中移除
     *
     * @param player 管理员
     */
    void removeAdmin(@NotNull APlayer player);

    /**
     * 获取聊天室中的管理员列表
     *
     * @return 聊天室中的管理员列表
     */
    @NotNull List<APlayer> getAdmins();

    /**
     * 获取聊天室的模型名称，面向程序，不能重复
     *
     * @return 聊天室的模型名称
     */
    @NotNull String getModelName();

    /**
     * 获取聊天室的显示名称，及 {@literal <DisplayName> message} 中的 Display
     *
     * @return 聊天室的显示名称
     */
    @NotNull String getDisPlayName();

    /**
     * 保存聊天室信息到文件
     *
     * @param path 文件路径
     * @implSpec 可以自定义文件格式和内容，但必须与 loadFromFile 保持一致
     */
    void saveToFile(@NotNull Path path);

    /**
     * 从文件中加载聊天室信息
     *
     * @param path 文件路径
     * @implSpec 可以自定义文件格式和内容，但必须与 saveToFile 保持一致
     */
    void loadFromFile(@NotNull Path path);

    void reloadConfig();
}
