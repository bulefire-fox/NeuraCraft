package com.bulefire.neuracraft.core.insidemodel.deepseek;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.AbsAgent;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.annotation.RegisterAgent;
import com.google.gson.Gson;
import lombok.*;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DeepSeek extends AbsAgent {
    private static final Logger log = getLogger(DeepSeek.class);

    @Setter
    @Getter
    private ChatHistory chatHistory;

    public DeepSeek(String name, UUID uuid, List<APlayer> players, List<APlayer> admins, String modelName, String disPlayName, int timePerMin) {
        super(name, uuid, players, admins, modelName, disPlayName, timePerMin);
        chatHistory = new ChatHistory();
        chatHistory.addBlock(
                new ChatHistory.ChatBlock(
                        "system",
                        DeepSeekConfig.getPrompt())
        );
    }

    private DeepSeek(DeepSeekSerializationData data) {
        super(data);
        chatHistory = data.chatHistory;
    }

    public DeepSeek() {
        super("DeepSeek1", UUID.randomUUID(), new ArrayList<>(), new ArrayList<>(), DeepSeekConfig.getModelName(), DeepSeekConfig.getDisplayName(), DeepSeekConfig.getTimePerMin());
        chatHistory = new ChatHistory();
    }

    @RegisterAgent
    public static void init() {
        log.info("DeepSeek static init");
        AgentController.registerAgentClassInitFunction(
                () -> {
                    var agentManager = AgentController.getAgentManager();
                    agentManager.registerAgentMapping("DeepSeek",
                            () -> new DeepSeek(
                                    "DeepSeek" + (new Random()).nextInt(),
                                    UUID.randomUUID(),
                                    new ArrayList<>(),
                                    new ArrayList<>(),
                                    DeepSeekConfig.getModelName(),
                                    DeepSeekConfig.getDisplayName(),
                                    DeepSeekConfig.getTimePerMin()
                            )
                    );

                    agentManager.registerAgentPathConsumer(
                            path -> {
                                if (path.toString().endsWith(".deepseek")) {
                                    return "DeepSeek";
                                }
                                return null;
                            }
                    );
                }
        );
        DeepSeekConfig.init();
    }

    @Override
    protected @NotNull String message(@NotNull String msg) {
        try {
            return decoder(
                    CUtil.AiPOST(
                            DeepSeekConfig.getUrl(),
                            buildBody(msg),
                            DeepSeekConfig.getToken()
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildBody(@NotNull String message) {
        log.info("start build body");
        Gson g = new Gson();
        chatHistory.addBlock(new ChatHistory.ChatBlock("user", message));
        return g.toJson(new SendBody(this.getModelName(), chatHistory.histories));
    }

    record SendBody(String model, List<ChatHistory.ChatBlock> messages) {
    }

    public String decoder(@NotNull String repose) {
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

    @Override
    @SneakyThrows
    public void saveToFile(@NotNull Path path) {
        FileUtil.saveJsonToFile(this, Path.of(path + ".deepseek"));
    }

    @Override
    public void loadFromFile(@NotNull Path path) {
        // 我简直是甜菜
        loadFileToManager(path, DeepSeekSerializationData.class, DeepSeek.class);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class DeepSeekSerializationData extends AbsAgent.AgentSerializationData {
        public ChatHistory chatHistory;

        public DeepSeekSerializationData(@NotNull DeepSeek agent) {
            super(agent);
            this.chatHistory = agent.chatHistory;
        }

        public DeepSeekSerializationData() {
            super();
            this.chatHistory = new ChatHistory();
        }
    }
}
