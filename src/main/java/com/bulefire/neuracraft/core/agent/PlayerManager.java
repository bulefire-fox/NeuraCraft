package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.compatibility.entity.APlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final Map<APlayer,UUID> players;

    public PlayerManager(Map<APlayer, UUID> players) {
        this.players = players;
    }

    public PlayerManager() {
        this.players = new HashMap<>();
    }

    public void addPlayer(APlayer player, UUID uuid) {
        if (players.containsKey(player)){
            return;
        }
        players.put(player, uuid);
    }

    public void updatePlayer(APlayer player, UUID uuid) {
        if (!players.containsKey(player)){
            addPlayer(player, uuid);
            return;
        }
        players.put(player, uuid);
    }

    public UUID getPlayerAgentUUID(APlayer player){
        return players.get(player);
    }

    public void removePlayer(APlayer player) {
        players.remove(player);
    }
}
