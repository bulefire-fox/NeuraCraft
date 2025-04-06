package com.bulefire.neuracraft.ai.control;

import com.bulefire.neuracraft.ai.control.player.PlayerControl;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;


public class NameManger {
    private static final Logger log = LogUtils.getLogger();

    /**
     * 获取聊天名称
     * @param username 玩家名称
     * @return 聊天室名称
     */
    public static @NotNull String getChatName(@NotNull String username){
        String chatName = Objects.requireNonNull(PlayerControl.get(username)).getChatName();
        log.info("get player {} in chat name: {}",username,chatName);
        return chatName;
    }
}
