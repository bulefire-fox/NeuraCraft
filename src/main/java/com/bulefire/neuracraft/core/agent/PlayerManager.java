package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.core.Agent;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PlayerManager
 * 玩家管理器, 用于管理玩家的 {@link Agent} 关系
 *
 * @author bulefire_fox
 * @version 1.0
 * @see Agent
 * @see APlayer
 * @see AgentController
 * @since 2.0
 */
@Log4j2
public class PlayerManager {
    private final Map<APlayer, UUID> players;

    public PlayerManager(Map<APlayer, UUID> players) {
        this.players = players;
    }

    public PlayerManager() {
        this.players = new HashMap<>();
    }

    /**
     * 添加玩家
     *
     * @param player {@linkplain APlayer 玩家对象}
     * @param uuid   玩家对应的 {@link Agent} 的 {@linkplain Agent#getUUID() UUID}
     * @apiNote 不会覆盖重复的 {@linkplain APlayer 玩家} 对应的 {@linkplain Agent#getUUID() UUID}. 如需更新玩家对应的 {@link Agent}, 请使用 {@link #updatePlayer(APlayer, UUID)}
     * @see PlayerManager#players
     * @see #updatePlayer(APlayer, UUID)
     */
    public void addPlayer(APlayer player, UUID uuid) {
        if (players.containsKey(player)) {
            return;
        }
        players.put(player, uuid);
    }

    /**
     * 更新玩家
     *
     * @param player {@linkplain APlayer 玩家对象}
     * @param uuid   玩家对应的 {@link Agent} 的 {@linkplain Agent#getUUID() UUID}
     * @apiNote 会覆盖重复的 {@linkplain APlayer 玩家} 对应的 {@linkplain Agent#getUUID() UUID}. 如需添加 {@linkplain APlayer 玩家}, 请使用 {@link #addPlayer(APlayer, UUID)}
     * @see PlayerManager#players
     * @see #addPlayer(APlayer, UUID)
     */
    public void updatePlayer(APlayer player, UUID uuid) {
        if (!players.containsKey(player)) {
            addPlayer(player, uuid);
            return;
        }
        players.put(player, uuid);
    }

    /**
     * 获取 {@linkplain APlayer 玩家} 对应的 {@link Agent} 的 {@linkplain Agent#getUUID() UUID}
     *
     * @param player {@linkplain APlayer 玩家}
     * @return {@linkplain APlayer 玩家} 对应的 {@link Agent} 的 {@linkplain Agent#getUUID() UUID}
     * @see PlayerManager#players
     */
    public UUID getPlayerAgentUUID(APlayer player) {
        return players.get(player);
    }

    /**
     * 移除{@linkplain APlayer 玩家}
     *
     * @param player {@linkplain APlayer 玩家}
     * @see PlayerManager#players
     */
    public void removePlayer(APlayer player) {
        players.remove(player);
    }

    /**
     * 从 {@link AgentManager} 中加载 {@linkplain APlayer 玩家}
     *
     * @param agentManager {@link AgentManager} 实例
     * @apiNote 底层使用 {@link #updatePlayer(APlayer, UUID)} 更新玩家
     * @see PlayerManager#players
     * @see AgentManager
     * @see #updatePlayer(APlayer, UUID)
     */
    public void loadPlayerFromAgentManager(@NotNull AgentManager agentManager) {
        for (var agent : agentManager.getAllAgents()) {
            for (var player : agent.getPlayers()) {
                log.info("Load player {} from agent {}", player.name(), agent.getName());
                updatePlayer(player, agent.getUUID());
            }
        }
    }
}
