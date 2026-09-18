package com.bulefire.neuracraft.core.inside.model.gpt;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.agent.AbsAgent;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.agent.annotation.RegisterAgent;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.inside.model.deepseek.ChatHistory;
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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class GPT extends AbsAgent {
    private static final Logger log = getLogger(GPT.class);
    private static final OpenAiCompatibleClient MODEL_CLIENT = new OpenAiCompatibleClient();

    private final ChatHistory chatHistory;

    public GPT(String name, UUID uuid, Set<APlayer> players, Set<APlayer> admins,
               String modelName, String displayName, int timePerMin) {
        super(name, uuid, players, admins, modelName, displayName, "gpt", timePerMin);
        chatHistory = new ChatHistory();
        chatHistory.addBlock(
                new ChatHistory.ChatBlock(
                        "system",
                        List.of(
                                new Content("text", GPTConfig.getPrompt()),
                                new Content("text", AgentController.fullRecommendedPrompt)
                        )
                )
        );
    }

    private GPT(GPTSerializationData data) {
        super(data);
        chatHistory = data.chatHistory == null ? new ChatHistory() : data.chatHistory;
        refreshSystemPrompt();
    }

    @Contract(" -> new")
    private static @NotNull GPT newInstance() {
        return new GPT(
                "GPT" + new Random().nextInt(),
                UUID.randomUUID(),
                new HashSet<>(),
                new HashSet<>(),
                GPTConfig.getModelName(),
                GPTConfig.getDisplayName(),
                GPTConfig.getTimePerMin()
        );
    }

    @RegisterAgent
    public static void init() {
        log.info("GPT static init");
        GPTConfig.init();
        AgentController.getInstance().registerAgentClassInitFunction(
                () -> {
                    var agentManager = AgentController.getInstance().getAgentManager();
                    agentManager.registerAgentMapping("GPT", GPT::newInstance);
                    agentManager.registerAgentPathConsumer(
                            path -> path.toString().endsWith(".gpt") ? "GPT" : null
                    );
                }
        );
    }

    @Override
    protected @NotNull String message(@NotNull AgentMessage message) {
        if (message.isToolResults()) {
            for (AgentMessage.ToolResult result : message.toolResults()) {
                chatHistory.addBlock(
                        ChatHistory.ChatBlock.tool(
                                result.toolCallId(),
                                result.content().stream()
                                        .map(Content::textOrThrow)
                                        .collect(java.util.stream.Collectors.joining("\n"))
                        )
                );
            }
        } else {
            refreshSystemPrompt(message.getText());
            chatHistory.addBlock(new ChatHistory.ChatBlock("user", message.getFormattedMessages()));
        }
        ChatResponse response = MODEL_CLIENT.send(new ChatRequest(
                GPTConfig.getUrl(),
                GPTConfig.getToken(),
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
        List<Content> systemContent = new java.util.ArrayList<>(List.of(
                new Content("text", GPTConfig.getPrompt()),
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
        FileUtil.saveJsonToFile(new GPTSerializationData(this), path);
    }

    @Override
    public void loadFromFile(@NotNull Path path) {
        loadFileToManager(path, GPTSerializationData.class, GPT.class);
    }

    @Override
    public void reloadConfig() {
        GPTConfig.init();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private static class GPTSerializationData extends AgentSerializationData {
        private ChatHistory chatHistory;

        public GPTSerializationData(@NotNull GPT agent) {
            super(agent);
            this.chatHistory = agent.chatHistory;
        }

        public GPTSerializationData() {
            super();
            this.chatHistory = new ChatHistory();
        }
    }
}
