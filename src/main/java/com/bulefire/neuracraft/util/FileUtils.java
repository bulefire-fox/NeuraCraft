package com.bulefire.neuracraft.util;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.ai.yy.ChatRoom;
import com.bulefire.neuracraft.ai.yy.ChatRoomManger;
import com.bulefire.neuracraft.ai.yy.save.ConfigFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class FileUtils {
    private static final Logger log = LogUtils.getLogger();

    public static final String rootS = NeuraCraft.configPath+"/neuracraft/";
    public static final Path root = Path.of(rootS);

    public static final String chatRoomS = rootS+"chatRoom/";
    public static final Path chatRoom = Path.of(chatRoomS);

    public static final String chatRomeFileS = chatRoomS+"chatRoom.json";
    public static final Path chatRomeFile = Path.of(chatRomeFileS);

    public static ConfigFile cf;

    /**
     * 初始化文件目录
     */
    public static void initFileAndDir(){
        log.info("initFileAndDir");
        if(!Files.exists(root)){
            try {
                Files.createDirectories(root);
                Files.createDirectories(chatRoom);
                Files.createFile(chatRomeFile);
                initJsonFile();
            } catch (Exception e) {
                log.info(e.toString());
                throw new RuntimeException(e);
            }
        }
        cf = loadConfigFileFromJsonFile();
    }

    public static void loadChatRoomToManager(@NotNull ChatRoomManger cm){
        log.info("start load room from file");
        Map<String, ChatRoom> clients = cm.getClients();
        for (ConfigFile.ChatRoomBean c : cf.getRoot()){
            log.info("load room: {}",c.getName());
            ChatRoom cr = ChatRoomConversion.beanToChatRoom(c);
            clients.put(cr.getName(),cr);
        }
        log.info("already load room from file");
    }

    /**
     * 初始化json文件
     */
    public static void initJsonFile() throws IOException {
        log.info("init Json");
        ConfigFile cf = new ConfigFile();
        cf.setRoot(new ArrayList<>());
        saveConfigFileToJsonFile(cf);
    }

    /**
     * 添加聊天室到管理并落地为json文件
     * @param cb chatRoomBean
     * @throws FileNotFoundException FileNotFoundException
     */
    public static void addChatRoom(@NotNull ConfigFile.ChatRoomBean cb) throws FileNotFoundException {
        log.info("start addChatRoom to root");
        log.info("already addChatRoom to root");
        if (cf != null) {
            cf.getRoot().add(cb);
            try {
                saveConfigFileToJsonFile(cf);
            } catch (IOException e) {
                log.error(e.toString());
                throw new RuntimeException(e);
            }
        }else{
            log.error("cf is null");
            try{
                initJsonFile();
            }catch (IOException e){
                log.error(e.toString());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 保存配置文件到json文件
     * @param cf ConfigFile
     * @throws IOException IOException
     */
    public static void saveConfigFileToJsonFile(@NotNull ConfigFile cf) throws IOException {
        log.info("try to save json to file");
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(chatRomeFileS)){
            log.info("save ConfigFile to file");
            g.toJson(cf, writer);
        }catch (IOException e){
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * 从json文件加载配置文件
     * @return ConfigFile
     */
    public static ConfigFile loadConfigFileFromJsonFile(){
        Gson g = new Gson();
        ConfigFile cf;
        try(FileReader reader = new FileReader(chatRomeFileS)){
            cf = g.fromJson(reader, ConfigFile.class);
        } catch (IOException e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
        return cf;
    }
}
