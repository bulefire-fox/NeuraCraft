package com.bulefire.neuracraft.core.inside.model.yinying;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.AbsAgent;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.annotation.RegisterAgent;
import com.google.gson.Gson;
import lombok.*;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class YinYing extends AbsAgent {
    private static final Logger log = getLogger(YinYing.class);

    private final String chatId;
    private final String appId;
    private final Variables variables;
    private final String systemPrompt;

    @Data
    static class Variables{
        private String nickName;
        private String furryCharacter;
        private String promptPatch;

        public Variables(String nickName, String furryCharacter, String promptPatch) {
            this.nickName = nickName;
            this.furryCharacter = furryCharacter;
            this.promptPatch = promptPatch;
        }
    }

    YinYing(String name, UUID uuid, List<APlayer> players, @NotNull List<APlayer> admins, String modelName, String disPlayName, int timePerMin,
                   String chatId, String appId, Variables variables, String systemPrompt) {
        super(name, uuid, players, admins, modelName, disPlayName, timePerMin);
        this.chatId = chatId;
        this.appId = appId;
        this.variables = variables;
        this.systemPrompt = systemPrompt;
    }

    private YinYing(YinYingSerializationData data) {
        super(data);
        chatId = data.chatId;
        appId = data.appId;
        variables = data.variables;
        systemPrompt = data.systemPrompt;
    }

    @Contract(" -> new")
    private static @NotNull YinYing newInstance(){
        var uuid = UUID.randomUUID();
        int ri = new Random().nextInt();
        return new YinYing(
                "YinYing" + ri,
                uuid,
                new ArrayList<>(),
                new ArrayList<>(),
                YinYingConfig.getModelName(),
                YinYingConfig.getDisplayName(),
                YinYingConfig.getTimePerMin(),
                YinYingConfig.getAppId()+'-'+uuid.toString().replace("-", "")+'-'+ri,
                YinYingConfig.getAppId(),
                YinYingConfig.getVariables(),
                YinYingConfig.getSystemPrompt()
        );
    }

    @RegisterAgent
    public static void init() {
        log.info("YinYing init");
        YinYingConfig.init();
        AgentController.registerAgentClassInitFunction(
                () -> {
                    var agentManager = AgentController.getAgentManager();
                    agentManager.registerAgentMapping("YinYing", YinYing::newInstance);
                    agentManager.registerAgentPathConsumer(
                            path -> {
                                if (path.toString().endsWith(".yinying")) {
                                    return "YinYing";
                                }
                                return null;
                            }
                    );
                }
        );
    }


    @Override
    protected @NotNull String message(@NotNull String msg) {
        var body = new SendBody(
                appId,
                chatId,
                new SendBody.Variables(
                        variables.getNickName(),
                        variables.getFurryCharacter(),
                        variables.getPromptPatch()
                ),
                getModelName(),
                systemPrompt,
                msg
        );
        Gson g = new Gson();
        String response;
        try {
            CUtil.Response rawresponse = CUtil.AiPOST(
                    YinYingConfig.getUrl(),
                    g.toJson(body),
                    YinYingConfig.getToken()
            );
            if (rawresponse.status() != 200) {
                return "API error with %s %s".formatted(rawresponse.status(), rawresponse.responseMessage());
            }
            response = rawresponse.response();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ResultBody result = g.fromJson(response, ResultBody.class);
        return result.getChoices().get(0).getMessage().getContent();
    }

    @Override
    @SneakyThrows
    public void saveToFile(@NotNull Path path) {
        FileUtil.saveJsonToFile(this, Path.of(path + ".yinying"));
    }

    @Override
    public void loadFromFile(@NotNull Path path) {
        loadFileToManager(path, YinYingSerializationData.class, YinYing.class);
    }

    @Override
    public void reloadConfig(){
        YinYingConfig.init();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class YinYingSerializationData extends AbsAgent.AgentSerializationData {
        private String chatId;
        private String appId;
        private Variables variables;
        private String systemPrompt;

        public YinYingSerializationData(@NotNull YinYing agent) {
            super(agent);
            this.chatId = agent.chatId;
            this.appId = agent.appId;
            this.variables = agent.variables;
            this.systemPrompt = agent.systemPrompt;
        }

        public YinYingSerializationData() {
            super();
            this.chatId = "";
            this.appId = "";
            this.variables = new Variables("", "", "");
            this.systemPrompt = "";
        }
    }
}
