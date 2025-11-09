package com.bulefire.neuracraft.ai.openaiAPI;

import com.bulefire.neuracraft.ai.AIChatRoom;
import com.bulefire.neuracraft.ai.AIModels;
import com.bulefire.neuracraft.ai.openaiAPI.config.OPAConfig;
import com.bulefire.neuracraft.ai.yy.SendBody;
import com.bulefire.neuracraft.config.opa.OPA;
import com.bulefire.neuracraft.util.AIHTTPClient;
import com.bulefire.neuracraft.util.FileUtils;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OPAChatRoom extends AIChatRoom {


    private static final Logger log = LoggerFactory.getLogger(OPAChatRoom.class);
    private ChatHistory chatHistory;
    private String chatModel;


    public OPAChatRoom(@NotNull String name, @NotNull List<String> playerList, @NotNull AIModels model, @NotNull List<String> adminList, @NotNull String dn, @NotNull ChatHistory c,@NotNull String chatModel) {
        super(name, playerList, model, adminList,dn);
        chatHistory = c;
        this.chatModel = chatModel;
        chatHistory.addBlock(new ChatHistory.ChatBlock("system", OPA.system_prompt));
    }

    public OPAChatRoom(@NotNull String name, @NotNull AIModels model, @NotNull String dn, @NotNull String chatModel) {
        super(name, model,dn);
        chatHistory = new ChatHistory();
        this.chatModel = chatModel;
        chatHistory.addBlock(new ChatHistory.ChatBlock("system", OPA.system_prompt));
    }

    public ChatHistory getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(ChatHistory chatHistory) {
        this.chatHistory = chatHistory;
    }

    @Override
    public @NotNull String sendMessage(@NotNull String message) {
        log.info("try to send message to chat room");
        String body = buildBody(message);
        log.info("body: {}", body);
        String repose;
        try {
            repose = AIHTTPClient.POST(OPA.api_url+OPA.api_interface, body, OPA.token);
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
            return "Error sending message";
        }
        return decoder(repose);
    }

    public String decoder(@NotNull String repose){
        if (repose.startsWith("POST request failed")) {
            log.error("Failed to get valid response from API: {}", repose);
            return "API Error: " + repose;
        }
        Gson g = new Gson();
        OPAResult result = g.fromJson(repose, OPAResult.class);
        OPAResult.ChoicesBean.Message m = result.getChoices().get(0).getMessage();
        chatHistory.addBlock(new ChatHistory.ChatBlock(m.getRole(), m.getContent()));
        return m.getContent();
    }

    private String buildBody(@NotNull String message){
        log.info("start build body");
        Gson g = new Gson();
        chatHistory.addBlock(new ChatHistory.ChatBlock("user", message));
        return g.toJson(new SendBody(this.chatModel,chatHistory.histories));
    }

    record SendBody(String model, List<ChatHistory.ChatBlock> messages){}

    @Override
    public void save() throws IOException {
        log.info("try to save chat room to file");
        String filename = this.model+"-"+this.chatModel.replace("/","=").replace(":","0")+"-"+this.name+".json";
        log.info("filename: {}", filename);
        OPAConfig configFile = new OPAConfig(this.name, this.playerList, this.model,this.adminList,this.disPlayName,this.chatModel,this.chatHistory);
        try{
            FileUtils.saveJsonToFile(configFile, FileUtils.chatPath.resolve(filename));
        }catch (IOException e){
            log.error("Error saving config file: {}", e.getMessage());
        }
    }

    @Override
    public void load(@NotNull Path path) throws IOException {
        log.info("load chat room from file");
        OPAConfig configFile;
        try {
            configFile = FileUtils.loadJsonFromFile(path, OPAConfig.class);
        } catch (NullPointerException e){
            log.error("load config file failed");
            return;
        }

        this.name = configFile.getName();
        this.playerList = configFile.getPlayerList();
        this.model = configFile.getModel();
        this.adminList = configFile.getAdminList();
        this.chatHistory = configFile.getChatHistory();
        this.chatModel = configFile.getChatModel();
        this.disPlayName = configFile.getDisPlayName();
        log.info("chat room name: {}", this.name);
    }

    @Override
    public void delete() throws IOException {
        String filename = this.model+"-"+this.chatModel.replace("/","=").replace(":","0")+"-"+this.name+".json";
        Files.deleteIfExists(FileUtils.chatPath.resolve(filename));
    }
}
