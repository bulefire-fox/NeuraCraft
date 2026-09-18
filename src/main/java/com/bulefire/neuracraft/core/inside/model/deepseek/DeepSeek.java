package com.bulefire.neuracraft.core.inside.model.deepseek;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.agent.AbsAgent;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.agent.annotation.RegisterAgent;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.inside.model.ChatRequest;
import com.bulefire.neuracraft.core.inside.model.ChatResponse;
import com.bulefire.neuracraft.core.inside.model.openai.OpenAiCompatibleClient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class DeepSeek extends AbsAgent {
    private static final Logger log = getLogger(DeepSeek.class);
    private static final OpenAiCompatibleClient MODEL_CLIENT = new OpenAiCompatibleClient();

    private final ChatHistory chatHistory;

    public DeepSeek(String name, UUID uuid, Set<APlayer> players, Set<APlayer> admins, String modelName, String disPlayName, int timePerMin) {
        super(name, uuid, players, admins, modelName, disPlayName, "deepseek", timePerMin);
        chatHistory = new ChatHistory();
        chatHistory.addBlock(
                new ChatHistory.ChatBlock(
                        "system",
                        List.of(
                                new Content("text", DeepSeekConfig.getPrompt()),
                                new Content("text", AgentController.fullRecommendedPrompt)
                        )
                )
        );
    }

    private DeepSeek(DeepSeekSerializationData data) {
        super(data);
        chatHistory = data.chatHistory == null ? new ChatHistory() : data.chatHistory;
        refreshSystemPrompt();
    }

    public DeepSeek() {
        super("DeepSeek1", UUID.randomUUID(), new HashSet<>(), new HashSet<>(), DeepSeekConfig.getModelName(), DeepSeekConfig.getDisplayName(), "deepseek", DeepSeekConfig.getTimePerMin());
        chatHistory = new ChatHistory();
    }

    @Contract(" -> new")
    private static @NotNull DeepSeek newInstance() {
        return new DeepSeek(
                "DeepSeek" + (new Random()).nextInt(),
                UUID.randomUUID(),
                new HashSet<>(),
                new HashSet<>(),
                DeepSeekConfig.getModelName(),
                DeepSeekConfig.getDisplayName(),
                DeepSeekConfig.getTimePerMin()
        );
    }

    @RegisterAgent
    public static void init() {
        log.info("DeepSeek static init");
        DeepSeekConfig.init();
        AgentController.getInstance().registerAgentClassInitFunction(
                () -> {
                    var agentManager = AgentController.getInstance().getAgentManager();
                    agentManager.registerAgentMapping("DeepSeek", DeepSeek::newInstance);

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
    }

    @Override
    protected @NotNull String message(@NotNull AgentMessage message) {
        log.info("start build body");
        if (message.isToolResults()) {
            for (AgentMessage.ToolResult result : message.toolResults()) {
                chatHistory.addBlock(
                        ChatHistory.ChatBlock.tool(
                                result.toolCallId(),
                                result.content().stream()
                                        .map(Content::textOrThrow)
                                        .collect(Collectors.joining("\n"))
                        )
                );
            }
        } else {
            refreshSystemPrompt(message.getText());
            chatHistory.addBlock(new ChatHistory.ChatBlock("user", message.getFormattedMessages()));
        }
        ChatResponse response = MODEL_CLIENT.send(new ChatRequest(
                DeepSeekConfig.getUrl(),
                DeepSeekConfig.getToken(),
                getModelName(),
                chatHistory.getHistories(),
                AgentController.getInstance().getMcpController().getOpenAiTools()
        ));
        chatHistory.addBlock(ChatHistory.ChatBlock.assistant(response));
        setToolCalls(response.toolCalls());
        return response.content() == null ? "" : response.content();
    }

    private void refreshSystemPrompt() {
        refreshSystemPrompt("");
    }

    private void refreshSystemPrompt(String userText) {
        String skillContext = AgentController.getInstance().getSkillManager().buildContext(userText);
        List<Content> systemContent = new ArrayList<>(List.of(
                new Content("text", DeepSeekConfig.getPrompt()),
                new Content("text", AgentController.fullRecommendedPrompt)
        ));
        if (!skillContext.isBlank()) {
            systemContent.add(new Content("text", skillContext));
        }
        chatHistory.replaceSystemContent(
                systemContent
        );
    }

    @Override
    @SneakyThrows
    public void saveToFile(@NotNull Path path) {
        log.debug("deepseek save to file: {}", path);
        FileUtil.saveJsonToFile(new DeepSeekSerializationData(this), path);
    }

    @Override
    public void loadFromFile(@NotNull Path path) {
        // 我简直是甜菜
        loadFileToManager(path, DeepSeekSerializationData.class, DeepSeek.class);
    }

    @Override
    public void reloadConfig() {
        DeepSeekConfig.init();
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
