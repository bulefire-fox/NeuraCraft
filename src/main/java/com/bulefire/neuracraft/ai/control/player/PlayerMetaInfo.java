package com.bulefire.neuracraft.ai.control.player;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlayerMetaInfo {
    private String ChatName;

    public PlayerMetaInfo() {
    }

    public PlayerMetaInfo(String chatName) {
        ChatName = chatName;
    }
}
