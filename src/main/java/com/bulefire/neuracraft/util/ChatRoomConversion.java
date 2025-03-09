package com.bulefire.neuracraft.util;

import com.bulefire.neuracraft.ai.yy.ChatRoom;
import com.bulefire.neuracraft.ai.yy.save.ConfigFile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChatRoomConversion {
    public static ConfigFile.@NotNull ChatRoomBean chatRoomToBean(@NotNull ChatRoom cr){
        return new ConfigFile.ChatRoomBean(cr.getName(), cr.getChatId(), cr.getPlayerList());
    }

    @Contract("_ -> new")
    public static @NotNull ChatRoom beanToChatRoom(@NotNull ConfigFile.ChatRoomBean cb){
        return new ChatRoom(cb.getName(),cb.getChatId(),cb.getPlayerList());
    }
}
