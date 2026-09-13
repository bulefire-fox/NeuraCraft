package com.bulefire.neuracraft.core.util;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import lombok.Getter;

public class PlayerNotHaveAgent extends RuntimeException {
    @Getter
    private final APlayer player;

    public PlayerNotHaveAgent(String message) {
        super(message);
        player = null;
    }

    public PlayerNotHaveAgent(String message, APlayer player) {
        super(message);
        this.player = player;
    }
}
