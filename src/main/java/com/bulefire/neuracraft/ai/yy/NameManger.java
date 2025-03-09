package com.bulefire.neuracraft.ai.yy;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;


public class NameManger {
    private static final Logger log = LogUtils.getLogger();

    /**
     * 获取聊天名称
     * @param username 玩家名称
     * @return 聊天室名称
     */
    public static @NotNull String getChatName(@NotNull String username){
        // TODO: 获取聊天室名称
        String chatName = "public";
        log.info("get player {} in chat name: {}",username,chatName);
        return chatName;
    }
}
