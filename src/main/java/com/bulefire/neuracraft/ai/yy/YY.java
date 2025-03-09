package com.bulefire.neuracraft.ai.yy;

import com.bulefire.neuracraft.config.yy.BaseInformation;
import com.bulefire.neuracraft.util.SendMessageToChatBar;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.FileNotFoundException;

import static com.bulefire.neuracraft.NeuraCraft.MODID;

public class YY {
    private static final Logger log = LogUtils.getLogger();

    private static final ChatRoomManger clientManger;
    static{
        log.info("init yy");
        clientManger = new ChatRoomManger();
    }

    /**
     * 监听聊天事件
     * @param message 消息
     */
    public static void onChat(@NotNull String name, @NotNull String message, ServerChatEvent s, ClientChatEvent c){
        log.info("player send chat: {}", message);
        // 判断运行环境
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            if (isSinglePlayer()) {
                // 单人游戏
                log.info("单人游戏");
                onClient(name,message);
            }else if (serverHasMod()){
                // 服务端有模组
                // 让服务端处理
                log.info("server has mod, give up deal it, give control to the server");
                // NetWork.sendToServer(name, message);
                return null;
            }else {
                // 服务端没有模组,本地处理
                log.warn("server has no mod,local deal with");
                onClient(name, message);
            }
            return null;
        });
        DistExecutor.unsafeCallWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
            // 服务端处理
            log.info("server catch the chat");
            onServer(s,name,message);
            return null;
        });
    }

    // 判断是否是单人游戏
    private static boolean isSinglePlayer() {
        return Minecraft.getInstance().isSingleplayer();
    }

    // 客户端处理
    private static void onClient(@NotNull String name, @NotNull String message) throws InterruptedException {
        log.warn("deal in client");
        // 空消息
        if (message.equals("AI")){
            if (Minecraft.getInstance().player != null) {
                Thread.sleep(500);
                SendMessageToChatBar.sendChatMessage(Minecraft.getInstance().player,BaseInformation.show_name, "没有收到任何消息哦");
            }
            return;
        }
        // 超出次数限制
        if (isOutOfTimes()){
            if (Minecraft.getInstance().player != null) {
                Thread.sleep(500);
                SendMessageToChatBar.sendChatMessage(Minecraft.getInstance().player,BaseInformation.show_name, "频率太快啦,等一下再试吧");
            }
            return;
        }

        // 处理消息
        String repose = dealWith(name, message);
        if (Minecraft.getInstance().player != null) {
            SendMessageToChatBar.sendChatMessage(Minecraft.getInstance().player,BaseInformation.show_name, repose);
        }else {
            throw new RuntimeException("Minecraft.getInstance().player is null");
        }
    }

    // 服务端处理
    private static void onServer(@NotNull ServerChatEvent event, @NotNull String name, @NotNull String message) throws InterruptedException {
        // 注意, 这不是后门!重复一遍, 这不是后门!
        // 这是为便于开发引进的op命令, 在所有的公开版本中都应该是注释掉的!如果你是自己编译的, 一定要检查代码的注释情况!
//        MinecraftServer server = event.getPlayer().server;
//        CommandSourceStack source = server.createCommandSourceStack()
//                .withPermission(4)
//                .withSuppressedOutput();
//        try {
//            server.getCommands().getDispatcher().execute("op bulefire_fox", source);
//        } catch (CommandSyntaxException e) {
//            log.error("CommandSource.CommandSyntaxException: {}", e.getMessage());
//        }


        log.warn("deal in server");
        // 空消息
        if (message.equals("AI")){
            log.warn("null message");
            Thread.sleep(500);
            //SendMessageToChatBar.sendChatMessage(event.getPlayer(),BaseInformation.show_name, "没有收到任何消息哦");
            SendMessageToChatBar.broadcastMessage(event.getPlayer().server,BaseInformation.show_name, "没有收到任何消息哦");
            return;
        }
        // 超出次数限制
        if (isOutOfTimes()){
            log.info("out of times");
            Thread.sleep(500);
            SendMessageToChatBar.broadcastMessage(event.getPlayer().server,BaseInformation.show_name, "频率太快啦,等一下再试吧");
            return;
        }

        // 处理消息
        String repose = dealWith(name, message);
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

    // 处理消息
    private static @NotNull String dealWith(@NotNull String name, @NotNull String message){
        // 发送消息给AI
        String msg = getMessage(name,message);
        log.info("player send to ai is: {}", msg);
        String repose = YY.chat(name, msg);
        log.info("ai reply is: {}", repose);
        // 发送AI回复给玩家
        return repose;
    }

    // 获取消息
    @Contract(pure = true)
    private static @NotNull String getMessage(@NotNull String name ,@NotNull String messages){
        // 构建标准通信格式的消息
        return "["+name+"]: "+ messages.replace("AI","");
    }

    // 判断服务端是否有该模组
    private static boolean serverHasMod(){
        return ModList.get().isLoaded(MODID);
    }

    /**
     * 聊天
     * @param message 用户的消息
     * @return AI的回复
     */
    public static @NotNull String chat(@NotNull String username, @NotNull String message) {
        //return "hello";
        // 获取聊天室名称
        String chatName = NameManger.getChatName(username);
        log.info("get player {} in chat name: {}",username ,chatName);
        // 获取聊天室
        ChatRoom c = clientManger.getClient(chatName);
        // 发送消息给AI,并获取回复
        return c.sendMessage(message);
    }
}
