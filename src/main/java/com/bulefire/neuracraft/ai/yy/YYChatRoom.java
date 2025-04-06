package com.bulefire.neuracraft.ai.yy;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.AIModels;
import com.bulefire.neuracraft.ai.yy.save.YYConfigFile;
import com.bulefire.neuracraft.config.yy.BaseInformation;
import com.bulefire.neuracraft.config.yy.Variables;
import com.bulefire.neuracraft.util.AIHTTPClient;
import com.bulefire.neuracraft.util.FileUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class YYChatRoom extends AIChatRoom {
    private static final Logger log = LogUtils.getLogger();

    /**
     * 聊天ID
     */
    private String chatId;

    private String error;
    private boolean toAdmin;

    public YYChatRoom(String name){
        super(name, AIModels.CyberFurry);
        // 生成聊天ID
        this.chatId = BaseInformation.appid + "-" +name + "-" +radomString();
    }

    public YYChatRoom(String name, AIModels model){
        super(name, model);
    }

    public YYChatRoom(String name, String chatId){
        super(name, AIModels.CyberFurry);
        this.chatId = chatId;
    }

    public YYChatRoom(String name, List<String> playerList, String chatId){
        super(name, playerList, AIModels.CyberFurry);
        this.chatId = chatId;
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

    @Override
    public void save() throws IOException {
        log.info("try to save chat room to file");
        String filename = this.model+"-"+this.name+".json";
        YYConfigFile configFile = new YYConfigFile(this.name, this.playerList, this.model,this.chatId);
        try {
            FileUtils.saveJsonToFile(configFile, FileUtils.chatPath.resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load(@NotNull Path path) throws IOException {
        YYConfigFile configFile;
        try {
            configFile = FileUtils.loadJsonFromFile(path, YYConfigFile.class);
        } catch (NullPointerException e){
            log.error("load config file failed");
            return;
        }
        this.name = configFile.getName();
        this.chatId = configFile.getChatId();
        this.playerList = configFile.getPlayerList();
        this.model = configFile.getModel();
    }

    @Override
    public void delete() throws IOException {
        String filename = this.model+"-"+this.name+".json";
        Files.deleteIfExists(FileUtils.chatPath.resolve(filename));
    }
}