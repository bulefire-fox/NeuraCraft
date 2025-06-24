package com.bulefire.neuracraft.ai.control;

import com.bulefire.neuracraft.ai.AI;
import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.control.player.PlayerControl;
import com.bulefire.neuracraft.config.yy.BaseInformation;
import com.bulefire.neuracraft.util.FileUtils;
import com.bulefire.neuracraft.util.SendMessageToChatBar;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;

import static com.bulefire.neuracraft.NeuraCraft.MODID;

public class AIControl {
    private static final Logger logger = LogUtils.getLogger();


    private static final ChatRoomManger cm;

    static {
        logger.info("load AI control");
        cm = new ChatRoomManger();
    }

    public static void init() throws IOException {
        logger.info("init AI control");
        cm.loadAllChatRoomFromFile(FileUtils.readAllFilePath(FileUtils.chatPath));
        PlayerControl.loadAllPlayerToChatRoom();
    }

    public static void onChat(@NotNull String name, @NotNull String message, @Nullable ServerChatEvent s, @Nullable ClientChatEvent c){
        logger.info("player send chat: {}", message);
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            if (isSinglePlayer()) {
                // 单人游戏
                logger.info("单人游戏");
                onClient(name,message);
            }else if (serverHasMod()){
                // 服务端有模组
                // 让服务端处理
                logger.info("server has mod, give up deal it, give control to the server");
                // NetWork.sendToServer(name, message);
                return null;
            }else {
                // 服务端没有模组,本地处理
                logger.warn("server has no mod,local deal with");
                onClient(name, message);
            }
            return null;
        });
        DistExecutor.unsafeCallWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            // 服务端处理
            logger.info("server catch the chat");
            if (s != null) {
                onServer(s,name,message);
            }else {
                logger.error("s is null, report to developer");
            }
            return null;
        });
    }
    // 判断是否是单人游戏
    private static boolean isSinglePlayer() {
        return Minecraft.getInstance().isSingleplayer();
    }

    // 客户端处理
    private static void onClient(@NotNull String name, @NotNull String message) throws InterruptedException {
        logger.warn("deal in client");
        // 处理消息
        MutableComponent repose = dealWith(name, message);

        String chatName = NameManger.getChatName(name);
        AIChatRoom c = cm.getClient(chatName);

        if (Minecraft.getInstance().player != null) {
            SendMessageToChatBar.sendChatMessage(Minecraft.getInstance().player,c.disPlayName, repose);
        }else {
            throw new RuntimeException("Minecraft.getInstance().player is null");
        }
    }

    // 服务端处理
    private static void onServer(@NotNull ServerChatEvent event, @NotNull String name, @NotNull String message) throws InterruptedException {
        logger.warn("deal in server");
        // 处理消息
        MutableComponent repose = dealWith(name, message);
        SendMessageToChatBar.broadcastMessage(event.getPlayer().server,BaseInformation.show_name, repose);
    }

    // 判断是否超出次数
    private static boolean isOutOfTimes(){
        if (Times.isTimes()){
            return true;
        }else {
            Times.add();
            return false;
        }
    }

    // 判断服务端是否有该模组
    private static boolean serverHasMod(){
        return ModList.get().isLoaded(MODID);
    }

    // 处理消息
    public static MutableComponent dealWith(@NotNull String name, @NotNull String message) throws InterruptedException {
        // 超出次数限制
        if (isOutOfTimes()){
            if (Minecraft.getInstance().player != null) {
                Thread.sleep(500);
            }
            return Component.translatable("neuracraft.chat.error.tooFast");
        }
        // 空消息
        if (message.equals("AI")){
            logger.warn("null message");
            Thread.sleep(500);
            return Component.translatable("neuracraft.chat.error.nullMessage");
        }
        // 发送消息给AI
        String msg = getMessage(name,message);
        logger.info("player send to ai is: {}", msg);
        MutableComponent repose = AIControl.chat(name, msg);
        logger.info("ai reply is: {}", repose);
        // 发送AI回复给玩家
        return repose;
    }

    // 处理控制消息
//    private static @NotNull String DealAICtl(@NotNull String name, @NotNull String message){
//        logger.info("catch player send control");
//        String[] args = message.split(" ");
//
//        if (args.length < 2){
//            return """
//                    how to use
//                    create <name> : create a chat room named <name>
//                    delete <name> : delete a chat room
//
//                    join <name>   : join a chat room
//                    exit <name>   : quit a chat room
//                    find          : find what chat room your in
//                    """;
//        }
//
//        if (args.length == 2){
//            if (args[1].equals("find")) {
//                try {
//                    AIChatRoom c = cm.getClient(NameManger.getChatName(name));
//                    return c.getName();
//                } catch (NoChatRoomFound e) {
//                    return "你没有加入任何聊天室";
//                }
//            }
//            return "请输入正确的指令";
//        }
//        return switch (args[1]) {
//            case "create" -> CommandDeal.create(name, args, cm);
//            case "delete" -> CommandDeal.delete(name, args, cm);
//            case "join" -> CommandDeal.join(name, args, cm);
//            case "exit" -> CommandDeal.exit(name, args, cm);
//            default -> "请输入正确的指令";
//        };
//    }

    // 获取消息
    @Contract(pure = true)
    private static @NotNull String getMessage(@NotNull String name ,@NotNull String messages){
        // 构建标准通信格式的消息
        return "["+name+"]: "+ messages.replace("AI","");
    }

    /**
     * 聊天
     *
     * @param message 用户的消息
     * @return AI的回复
     */
    public static MutableComponent chat(@NotNull String username, @NotNull String message) {
        // 获取聊天室名称
        String chatName = NameManger.getChatName(username);
        logger.info("chat get player {} in chat name: {}",username ,chatName);
        // 获取聊天室
        AI c;
        try {
            c = cm.getClient(chatName);
        }catch (NoChatRoomFound e){
            logger.error("chat NoChatRoomFound: {}", e.getMessage());
            cm.printAllRooms();
            return Component.translatable("neuracraft.chat.error.notInChatRoom").append(Component.translatable("neuracraft.chat.error.notInChatRoom.help"));
        }
        // 发送消息给AI,并获取回复
        String msg = c.sendMessage(message);
        logger.info("ai replay is: {}", msg);
        try {
            c.save();
        }catch (IOException e){
            logger.error("IOException: {}", e.getMessage());
        }
        return Component.literal(msg);
    }


    public static ChatRoomManger getCm() {
        return cm;
    }
}
