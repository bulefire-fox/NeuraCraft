package com.bulefire.neuracraft.ai.yy;

import com.bulefire.neuracraft.ai.yy.save.ConfigFile;
import com.bulefire.neuracraft.util.FileUtils;
import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomManger {
    private static final Logger log = LogUtils.getLogger();

    /**
     * 客户端表
     */
    private final Map<String, ChatRoom> clients;

    ChatRoomManger(){
        // 初始化客户端表
        clients = new HashMap<>();
        // 加载聊天室从文件
        FileUtils.loadChatRoomToManager(this);
        // 初始化一个公共聊天室
        createClient("public");
    }

    public static ChatRoom loadChatRoomFromFile(@NotNull File file){
        return null;
    }

    /**
     * 获取一个聊天室
     * @param chatName 聊天室名称
     * @return 聊天室实例
     */
    public ChatRoom getClient(String chatName) {
        if (!clients.containsKey(chatName)){
            log.info("client: {}  not exists",chatName);
            createClient(chatName);
        }
        // 返回客户端
        log.info("get client: {}",chatName);
        return clients.get(chatName);
    }

    /**
     * 创建一个聊天室
     * @param chatName 聊天室名称
     * @return 聊天室实例
     */
    public boolean createClient(String chatName) {
        // 如果客户端表里已经有这个客户端了，就返回false
        if(clients.containsKey(chatName)){
            log.info("client: {} is exists",chatName);
            return false;
        }
        // 创建一个客户端
        clients.put(chatName,new ChatRoom(chatName));
        // 落地到文件
        log.info("start create client: {}",chatName);
        ConfigFile.ChatRoomBean cb = new ConfigFile.ChatRoomBean();
        cb.setName(chatName);
        cb.setChatId(clients.get(chatName).getChatId());
        cb.setPlayerList(clients.get(chatName).getPlayerList());
        try {
            FileUtils.addChatRoom(cb);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        log.info("already create client: {}",chatName);
        // 返回true
        return true;
    }

    public Map<String, ChatRoom> getClients() {
        return clients;
    }
}