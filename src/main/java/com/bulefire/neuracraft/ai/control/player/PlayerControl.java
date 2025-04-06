package com.bulefire.neuracraft.ai.control.player;

import com.bulefire.neuracraft.ai.control.player.config.PlayerConfigFile;
import com.bulefire.neuracraft.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerControl {
    private final static Logger logger = LoggerFactory.getLogger(PlayerControl.class);

    private final static @NotNull Map<String, PlayerMetaInfo> players = new HashMap<>();

    public static void put(@NotNull String name, @NotNull PlayerMetaInfo p){
        logger.info("put player: {}", name);
        if (players.containsKey(name)){
            logger.info("player already exist");
            return;
        }
        players.put(name, p);
    }

    public static @Nullable PlayerMetaInfo get(@NotNull String name){
        logger.info("get player: {}", name);
        return players.get(name);
    }

    public static void loadAllPlayerToChatRoom() throws IOException {
        logger.info("load all player from file");
        logger.info("load all player to memory");
        List<Path> paths = FileUtils.readAllFilePath(FileUtils.playerPath);
        for (Path path : paths){
            logger.info("load player: {}", path.toFile().getName());
            PlayerConfigFile pc = FileUtils.loadJsonFromFile(path, PlayerConfigFile.class);
            put(pc.getName(), new PlayerMetaInfo(pc.getChatRoom()));
        }
    }

    public static void saveAllPlayerToFile(@NotNull List<String> players_) throws IOException{
        logger.info("save all player to file {}", players_);
        for (String player : players_){
            PlayerMetaInfo pm = players.get(player);
            PlayerConfigFile pc = new PlayerConfigFile();
            pc.setName(player);
            pc.setChatRoom(pm.getChatName());
            FileUtils.saveJsonToFile(pc, FileUtils.playerPath.resolve(player + ".json"));
        }
    }
}
