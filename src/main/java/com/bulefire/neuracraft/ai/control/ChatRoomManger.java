package com.bulefire.neuracraft.ai.control;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.AIModels;
import com.bulefire.neuracraft.ai.yy.YYChatRoom;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomManger {
    private static final Logger logger = LogUtils.getLogger();

    /**
     * 客户端表
     */
    private final Map<String, AIChatRoom> clients;

    public ChatRoomManger(@NotNull Map<String, AIChatRoom> clients) {
        this.clients = clients;
    }

    public ChatRoomManger() {
        this.clients = new HashMap<>();
    }

    public AIChatRoom getClient(@NotNull String chatName) throws NoChatRoomFound{
        if (clients.containsKey(chatName)){
            return clients.get(chatName);
        }
        throw new NoChatRoomFound("聊天室不存在");
    }

    void printAllRooms(){
        logger.warn(clients.keySet().toString());
    }

    public boolean createClient(@NotNull String chatName, @NotNull AIModels model) {
        logger.info("create client {} model {}", chatName, model);
        if (clients.containsKey(chatName)){
            logger.warn("client {} already exists", chatName);
            return false;
        }

        if (model == AIModels.CyberFurry) {
            clients.put(chatName, new YYChatRoom(chatName));
            return true;
        }

        return false;
    }

    public void removeClient(@NotNull String chatName) {
        logger.info("remove client {}", chatName);
        clients.remove(chatName);
    }

    public void loadAllChatRoomFromFile(@NotNull List<Path> paths) throws IOException {
        logger.info("load all chat room from file");
        String m;
        for (Path path : paths){
            m = path.toFile().getName().split("-")[0];
            AIModels model = AIModels.getModel(m);
            if (model == null){
                logger.error("model {} not found", m);
                return;
            }
            AIChatRoom client = new YYChatRoom(path.toFile().getName().split("-")[1],model);
            client.load(path);
            logger.info("load client {}", client.getName());
            this.clients.put(client.getName(), client);
        }
    }
}