package com.bulefire.neuracraft.ai.control.command;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.AIModels;
import com.bulefire.neuracraft.ai.control.ChatRoomManger;
import com.bulefire.neuracraft.ai.control.NameManger;
import com.bulefire.neuracraft.ai.control.NoChatRoomFound;
import com.bulefire.neuracraft.ai.control.player.PlayerControl;
import com.bulefire.neuracraft.ai.control.player.PlayerMetaInfo;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CommandDeal {
    private static final Logger logger = LogUtils.getLogger();

    public static @NotNull String create(@NotNull String name, String @NotNull [] args, @NotNull ChatRoomManger cm){
        String cname = args[2];
        if (cname.isEmpty()){
            return "请输入正确的指令";
        }
        logger.info("start create room");
        if(!cm.createClient(cname, AIModels.CyberFurry)){
            return "聊天室已经存在";
        }

        try {
            cm.getClient(cname).playerList.add(name);
        } catch (NoChatRoomFound e) {
            logger.error(" NoChatRoomFound: {}", e.getMessage());
            return "创建失败";
        }


        if (PlayerControl.get(name) == null){
            return "创建失败";
        }

        Objects.requireNonNull(PlayerControl.get(name)).setChatName(cname);
        try {
            PlayerControl.saveAllPlayerToFile(List.of(name));
        }catch (IOException e){
            logger.error("save player IOException: {}", e.getMessage());
        }
        logger.info("control create {}",cname);
        return "已创建聊天室";
    }

    public static @NotNull String delete(@NotNull String name, String @NotNull [] args, @NotNull ChatRoomManger cm){
        AIChatRoom c = cm.getClient(args[2]);
        try {
            c.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cm.removeClient(args[2]);
        return "已删除聊天室 " + args[2];
    }

    public static @NotNull String join(@NotNull String name, String @NotNull [] args, @NotNull ChatRoomManger cm){
        logger.info("start join room");
        String chat = args[2];

        AIChatRoom new_c;
        try {
            new_c = cm.getClient(chat);
        } catch (NoChatRoomFound e) {
            return "聊天室 " + chat + " 不存在";
        }

        AIChatRoom old_c;
        try {
            old_c = cm.getClient(NameManger.getChatName(name));
        } catch (NoChatRoomFound e) {
            return "聊天室 " + chat + " 不存在";
        }
        old_c.playerList.remove(name);


        new_c.playerList.add(name);
        PlayerMetaInfo m = PlayerControl.get(name);
        if (m != null) {
            m.setChatName(chat);
        }
        try {
            old_c.save();
            new_c.save();
            PlayerControl.saveAllPlayerToFile(List.of(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "已加入 "+chat+" 聊天室";
    }

    public static @NotNull String exit(@NotNull String name, String @NotNull [] args, @NotNull ChatRoomManger cm){
        String cname = NameManger.getChatName(name);
        if (!cname.equals(args[2])){
            return "你不在这个聊天室中!";
        }
        AIChatRoom c = cm.getClient(cname);
        c.playerList.remove(name);
        PlayerMetaInfo m = PlayerControl.get(name);
        if (m != null) {
            m.setChatName(null);
        }
        return "已退出"+args[2]+"聊天室";
    }
}
