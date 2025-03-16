package com.bulefire.neuracraft.ai.yy;

import com.bulefire.neuracraft.config.yy.BaseInformation;
import com.bulefire.neuracraft.config.yy.Variables;
import com.bulefire.neuracraft.util.AIHTTPClient;
import com.bulefire.neuracraft.util.SendMessageToChatBar;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;

public class ChatRoom {
    private static final Logger log = LogUtils.getLogger();

    /**
     * 名字
     */
    private final String name;
    /**
     * 聊天ID
     */
    private final String chatId;

    private List<String> playerList;

    private String error;
    private boolean toAdmin;

    public ChatRoom(String name){
        // 设置名字
        this.name = name;
        // 生成聊天ID
        this.chatId = BaseInformation.appid + "-" +name + "-" +radomString();
        this.playerList = new ArrayList<>();
    }

    public ChatRoom(String name, String chatId){
        this.name = name;
        this.chatId = chatId;
        this.playerList = new ArrayList<>();
    }

    public ChatRoom(String name, String chatId, List<String> playerList){
        this.name = name;
        this.chatId = chatId;
        this.playerList = playerList;
    }

    public String getChatId() {
        return chatId;
    }

    /**
     * 生成随机字符串
     * @return 随机字符串
     */
    @Contract(pure = true)
    private @NotNull String radomString() {
        return UUID.randomUUID().toString().replace("-","");
    }

    /**
     * 发送消息给AI
     * @param message 消息
     * @return 回复
     */
    public @NotNull String sendMessage(@NotNull String message) {
        // 构建请求体
        String body = buildBody(message);
        // 发送请求
        try {
            String response = AIHTTPClient.POST(BaseInformation.api_url+BaseInformation.api_interface, body);
            // 检查请求体
            if (!checkBody(response)){
                if (toAdmin){
                    return "Error, 请联系管理员 \n"+error;
                }
                return error;
            }
            // 获取回复并解析
            return getReply(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查请求体
     * @param response 请求体
     * @return 是否正常
     */
    private boolean checkBody(String response) {
        log.info("check body");
        try {
            // 如果是JSON格式，则继续执行
            JsonParser.parseString(response);
            // appId不存在
            if (response.contains("""
                    {
                      "status": false,
                      "data": "Error: appId不存在！"
                    }""")){
                sendToPlayer("Error: appId不存在！");
                error = "appId不存在！";
                toAdmin = true;
                log.error("appId不存在！ \n {}", response);
                return false;
            // token不存在
            }else if (response.contains("""
                    {
                      "status": false,
                      "data": "Error: 凭据无效！"
                    }
                    """)) {
                sendToPlayer("Error: token不存在！");
                error = "token不存在！";
                toAdmin = true;
                log.error("token不存在！\n {}", response);
                return false;
                // 合法
            }else if(response.contains("""
                    {
                    "error": "Rate limit exceeded. Please try again later."
                    }
                    """)){
                error = "频率太快啦,等一下再试吧";
                toAdmin = false;
                return false;
            } else {
                return true;
            }
        } catch (JsonSyntaxException e) {
            if (response.contains("POST request failed with response code:")){
                error = response;
                toAdmin = true;
                return false;
            }
            // 如果解析失败，说明响应不是有效的 JSON
            sendToPlayer("Error: 请求失败！" + response);
            error = "Error: 请求失败！" + response;
            toAdmin = true;
            log.error("Response is not a valid JSON: {}", response);
            return false;
        }
    }

    private void sendToPlayer(String message){
//        if (this.serverPlayer != null){
//            SendMessageToChatBar.sendChatMessage(this.serverPlayer, BaseInformation.show_name, message);
//        }else {
//            SendMessageToChatBar.sendChatMessage(BaseInformation.show_name,message);
//        }
    }

    /**
     * 解析并获取回复
     * @param response 请求体
     * @return 回复
     */
    private @NotNull String getReply(@NotNull String response){
        log.info("start get reply");
        Gson gson = new Gson();
        // 反序列化
        ReplyBody replyBody = gson.fromJson(response, ReplyBody.class);
        log.info("replyBody: {}", replyBody);
        return replyBody.getChoices().get(0).getMessage().getContent();
    }

    /**
     *  构建请求体
     * @param message 消息
     * @return 请求体
     */
    private @NotNull String buildBody(@NotNull String message){
        log.info("start build body");
        SendBody body = new SendBody();
        // 设置请求体
        body.setAppId(BaseInformation.appid);
        body.setChatId(this.chatId);
        body.setModel(BaseInformation.model);
        body.setSystemPrompt(BaseInformation.system_prompt);
        body.setMessage(message);
        log.info("start build variables");
        body.getVariables().setNickName(Variables.nickname);
        body.getVariables().setFurryCharacter(Variables.furry_charter);
        body.getVariables().setPromptPatch(Variables.prompt_patch);

        Gson gson = new Gson();
        // log.info(gson.toJson(body));
        // SendMessageToChatBar.sendChatMessage(BaseInformation.show_name, gson.toJson(body));
        // 序列化
        log.info("build body is: {}", gson.toJson(body));
        return gson.toJson(body);
    }

    public List<String> getPlayerList() {
        return playerList;
    }

    public String getName() {
        return name;
    }
}
