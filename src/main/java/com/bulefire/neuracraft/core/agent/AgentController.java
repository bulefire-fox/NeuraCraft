package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.NeuraCraft;
import com.bulefire.neuracraft.compatibility.command.CommandRegister;
import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.compatibility.entity.SendMessage;
import com.bulefire.neuracraft.compatibility.function.process.*;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.compatibility.util.scanner.AnnotationsMethodScanner;
import com.bulefire.neuracraft.core.agent.annotation.RegisterAgent;
import com.bulefire.neuracraft.core.agent.commnd.NCCommand;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.agent.entity.AgentResponse;
import com.bulefire.neuracraft.core.command.GameCommand;
import com.bulefire.neuracraft.core.config.NCMainConfig;
import com.bulefire.neuracraft.core.mcp.MCPController;
import com.bulefire.neuracraft.core.inside.model.ModelException;
import com.bulefire.neuracraft.core.skill.SkillManager;
import com.bulefire.neuracraft.core.util.AgentOutOfTime;
import com.bulefire.neuracraft.core.util.UnSupportFormattedMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * AgentController
 * 用于管理和调度所有的 {@link Agent} 实例, 与玩家进行交互, 监听事件并处理.
 *
 * @author bulefire_fox
 * @version 2.0
 * @see Agent
 * @see AgentManager
 * @see PlayerManager
 * @see GameCommand
 * @see ChatEventProcesser.ChatMessage
 * @see AgentController#registerAgentClassInitFunction(Runnable)
 * @since 2.0
 */
@Log4j2
public class AgentController {
    private static final Pattern JOIN_MSG_P = Pattern.compile("^[a-zA-Z0-9_]+\\\\s+join\\\\s+the\\\\s+game\\\\.\\\\.$");
    private static final Pattern LEAVE_MSG_P = Pattern.compile("^[a-zA-Z0-9_]+\\\\s+left\\\\s+the\\\\s+game\\\\.\\\\.$");
    
    public static final Function<String, Boolean> JOIN_MSG_FORMATE = (s) -> JOIN_MSG_P.matcher(s).matches();
    public static final Function<String, Boolean> LEAVE_MSG_FORMATE = (s) -> LEAVE_MSG_P.matcher(s).matches();
    
    public static final Consumer<Component> gameLogger = (c) -> CUtil.broadcastMessageToCharBar(new SendMessage(c, CUtil.getCurrentEnv(), CUtil.broadcast));
    // region prompt — loaded from NCMainConfig, hot-reloadable via refreshPrompts()
    public static String AgentIdentityPrompt;
    public static String AliveMCPPromptHead;
    public static String AliveMCPPrompt;
    public static String MCPFormatPrompt;
    public static String AgentMCPOutputPrompt;
    public static String AgentMCPWorkflowPrompt;
    public static String reiteratedKeyPrompt;
    public static String fullRecommendedPrompt;
    // endregion
    @Getter
    private final AgentManager agentManager;
    @Getter
    private final PlayerManager playerManager;
    @Getter
    private final GameCommand GAME_COMMAND;
    @Getter
    private final MCPController mcpController;
    @Getter
    private final SkillManager skillManager;
    
    private final List<Runnable> agentClassInitFunctions;
    
    @Setter
    private String prefix;
    
    private volatile ExecutorService executor;
    private volatile boolean acceptingMessages;
    
    public static final AgentController INSTANCE = new AgentController();
    private AgentController() {
        agentManager = new AgentManager();
        playerManager = new PlayerManager();
        GAME_COMMAND = GameCommand.getINSTANCE();
        mcpController = MCPController.getInstance();
        skillManager = SkillManager.getInstance();
        agentClassInitFunctions = Collections.synchronizedList(new ArrayList<>());
        prefix = NCMainConfig.getPrefix();
        executor = createExecutor();
        acceptingMessages = true;
    }
    public static AgentController getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册一个 Agent 类初始化逻辑
     *
     * @param fun 初始化逻辑, 包装为一个 {@link Runnable}
     * @see Runnable
     * @see AgentController#agentClassInitFunctions
     */
    public synchronized void registerAgentClassInitFunction(Runnable fun) {
        log.debug("register agent class init function {}", fun);
        agentClassInitFunctions.add(fun);
    }
    
    /**
     * 从 NCMainConfig 重新加载提示词并拼接完整提示词. 供 FileWatcher 热重载使用.
     */
    public static void refreshPrompts() {
        var p = NCMainConfig.getPrompts();
        AgentIdentityPrompt   = p.getAgentIdentity();
        AliveMCPPromptHead   = p.getAliveMcpHead();
        MCPFormatPrompt      = p.getMcpFormat();
        AgentMCPOutputPrompt = p.getMcpOutput();
        AgentMCPWorkflowPrompt = p.getMcpWorkflow();
        reiteratedKeyPrompt  = p.getReiteratedKey();

        // OpenAI-compatible agents receive tool definitions in the request's
        // native `tools` field, so do not duplicate them in the system prompt.
        AliveMCPPrompt = "";

        fullRecommendedPrompt =
                AgentIdentityPrompt +
                        AliveMCPPromptHead +
                        AliveMCPPrompt +
                        MCPFormatPrompt +
                        AgentMCPOutputPrompt +
                        AgentMCPWorkflowPrompt +
                        reiteratedKeyPrompt;
        log.info("Prompts refreshed from config");
    }

    public static void init() {
        // 加载配置文件

        log.debug("register to chatEvent");
        // 监听聊天事件
        ChatEventProcesser.registerFun(getInstance()::onMessage);
        // 监听玩家加入事件
        PlayerJoinEventProcesser.registerFun(
                (msg) -> {
                    String message = NCMainConfig.getPrefix() + msg.player().toFormatedString() + "join the game..";
                    // 稍加处理即可
                    getInstance().onMessage(
                            new ChatEventProcesser.ChatMessage(
                                    message,
                                    msg.player(),
                                    msg.env()
                            )
                    );
                }
        );
        // 监听玩家退出事件
        PlayerExitEventProcesser.registerFun(
                (msg) -> {
                    UUID uuid = getInstance().playerManager.getPlayerAgentUUID(msg.player());
                    if (uuid != null && getInstance().agentManager.getAgent(uuid) != null) {
                        String message = NCMainConfig.getPrefix() + msg.player().toFormatedString() + "exit the game..";
                        getInstance().onMessage(
                                new ChatEventProcesser.ChatMessage(
                                        message,
                                        msg.player(),
                                        msg.env()
                                )
                        );
                    }
                    getInstance().playerManager.removePlayer(msg.player());
                }
        );
        // 监听服务器关闭事件
        // 保存所有Agent
        ServerStoppingEventProcesser.registerFun(
                getInstance()::saveAllAgentToFile
        );
        // 注册我们自己的指令
        //CommandRegister.registerCommand(NCCommand.getCommands());
        NCCommand.buildCommands();
        
        // 扫描插件的注册
        // 放在InitCore里了
        
        // 扫描MCP的注册
        // 放在InitCore里了
        
        refreshPrompts();
        
        // 扫描我们自己的Agent类
        var methods = AnnotationsMethodScanner.scanPackageToMethod("com.bulefire.neuracraft.core", Set.of(RegisterAgent.class));
        log.info("MMM found methods {}", methods);
        
        for (Method method : methods) {
            try {
                method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Failed to invoke method , report to the mod develop, not NC!", e);
                throw new RuntimeException(e);
            }
        }
        
        // 执行所有Agent类的初始化逻辑
        List<Runnable> copy = new ArrayList<>(getInstance().agentClassInitFunctions);
        log.debug("all functions {}", copy);
        for (Runnable fun : copy) {
            log.debug("Running agent class init function {}", fun);
            fun.run();
        }
        // 加载所有的指令
        //CommandRegister.registerCommands(agentGameCommand.getAllCommands());
        CommandRegister.registerCommands(getInstance().GAME_COMMAND.getAllCommands());
    }
    
    public static void afterInit() {
        getInstance().start();
        // 加载所有Agent,必须在Agent类加载完后加载,否则无法注入Function
        getInstance().loadAllAgentFromFile();
        // 加载玩家
        getInstance().playerManager.loadPlayerFromAgentManager(getInstance().agentManager);
    }
    
    public void onMessage(@NotNull ChatEventProcesser.ChatMessage chatMessage) {
        if (!submitAsync(() -> AsyncMessage(chatMessage)) && acceptingMessages) {
            log.warn("Could not schedule agent message because the executor is unavailable");
        }
    }

    /**
     * Submit non-Minecraft work to the agent worker pool without blocking the caller.
     *
     * @return whether the task was accepted
     */
    public boolean submitAsync(@NotNull Runnable task) {
        if (!acceptingMessages) {
            log.debug("Ignoring async task while AgentController is stopping");
            return false;
        }
        try {
            executor.submit(task);
            return true;
        } catch (RejectedExecutionException e) {
            if (acceptingMessages) {
                log.warn("Could not schedule async agent task because the executor is unavailable");
            } else {
                log.debug("Ignoring async task rejected during shutdown");
            }
            return false;
        }
    }
    
    // 消息入口
    private void AsyncMessage(@NotNull ChatEventProcesser.ChatMessage chatMessage) {
        processMessage(chatMessage);
    }

    private void processMessage(@NotNull ChatEventProcesser.ChatMessage chatMessage) {
        if (!acceptingMessages)
            return;
        if (! chatMessage.msg().startsWith(prefix))
            return;
        String msg = chatMessage.msg().substring(prefix.length());
        if (Objects.requireNonNull(chatMessage.env()) == ChatEventProcesser.ChatMessage.Env.CLIENT) {
            if (CUtil.hasMod.apply(NeuraCraft.MOD_ID)) {
                // 服务端有模组,交给服务端处理
                return;
            }
        }
        // 单人和服务器唯一的区别是发送消息至聊天栏的方式,但这是兼容层的事情,在这里我们把他当作同一个东西
        // 当服务器没有模组时当作单人处理
        // 保留 SINGLE 的唯一目的是在调用兼容层方法时作为环境传入,以便兼容层做出合理的处理
        
        // uuid 最为 agent 的唯一标识符
        UUID uuid = playerManager.getPlayerAgentUUID(chatMessage.player());
        if (uuid == null) {
            // 玩家不在聊天室则 uuid 为 null
            // 提示即可
            CUtil.sendMessageToPlayer(
                    new SendMessage(
                            Component.translatable("neuracraft.agent.command.find.notInChatRoom"),
                            chatMessage.env(),
                            chatMessage.player()
                    )
            );
            return;
        }
        // 获取聊天室
        Agent agent = agentManager.getAgent(uuid);
        if (agent == null) {
            playerManager.removePlayer(chatMessage.player());
            CUtil.sendMessageToPlayer(
                    new SendMessage(
                            Component.translatable("neuracraft.agent.command.find.notInChatRoom"),
                            chatMessage.env(),
                            chatMessage.player()
                    )
            );
            return;
        }
        
        if (agent.isMCPCalling()) {
            CUtil.broadcastMessageToGroupPlayer(
                    new SendMessage(
                            Component.translatable("neuracraft.agent.chat.error.mcpcalling"),
                            chatMessage.env(),
                            chatMessage.player()
                    ),
                    agent.getPlayers().stream().toList()
            );
            return;
        }
        
        if (!agent.tryLockMessageLock()) {
            CUtil.broadcastMessageToGroupPlayer(
                    new SendMessage(
                            Component.translatable("neuracraft.agent.chat.error.locked"),
                            chatMessage.env(),
                            chatMessage.player()
                    ),
                    agent.getPlayers().stream().toList()
            );
            return;
        }
        try {
            // 防止多重调用的幻觉
            if (agent.isMCPCalling()) {
                CUtil.broadcastMessageToGroupPlayer(
                        new SendMessage(
                                Component.translatable("neuracraft.agent.chat.error.mcpcalling"),
                                chatMessage.env(),
                                chatMessage.player()
                        ),
                        agent.getPlayers().stream().toList()
                );
                return;
            }

            AgentResponse remessage;
            try {
                remessage = agent.sendMessage(new AgentMessage(List.of(new Content("text", msg)), chatMessage.player()));
            } catch (AgentOutOfTime e) {
                CUtil.broadcastMessageToGroupPlayer(
                        new SendMessage(
                                Component.translatable("neuracraft.agent.chat.error.tooFast"),
                                chatMessage.env(),
                                chatMessage.player()
                        ),
                        agent.getPlayers().stream().toList()
                );
                return;
            } catch (UnSupportFormattedMessage e) {
                CUtil.broadcastMessageToGroupPlayer(
                        new SendMessage(
                                Component.translatable("neuracraft.agent.chat.warn.unsupport.message.type", agent.getName(), e.getMessageType().name()),
                                chatMessage.env(),
                                chatMessage.player()
                        ),
                        agent.getPlayers().stream().toList()
                );
                return;
            }

            if (remessage.state() == AgentResponse.State.START_MCP_CALL || remessage.state() == AgentResponse.State.MCP_CALLING) {
                remessage = mcpCall(
                        agent, remessage,
                        (input) -> CUtil.broadcastMessageToGroupPlayer(
                                new SendMessage(
                                        Component.translatable(
                                                "neuracraft.mcp.message.format.system",
                                                Component.literal(agent.getDisPlayName()).withStyle(
                                                        style -> style.withColor(TextColor.fromRgb(0x7CFC00))
                                                ),
                                                input
                                        ),
                                        chatMessage.env(),
                                        chatMessage.player()
                                ),
                                agent.getPlayers().stream().toList()
                        )
                );
            }

            if (!acceptingMessages)
                return;
            agent.saveToFile(getAgentPath(agent));

            CUtil.broadcastMessageToGroupPlayer(
                    new SendMessage(
                            Component.translatable("neuracraft.agent.chat.message.format.player", agent.getDisPlayName(), remessage.msg()),
                            chatMessage.env(),
                            chatMessage.player()
                    ),
                    agent.getPlayers().stream().toList()
            );
        } catch (RuntimeException e) {
            agent.abortMessage();
            if (e instanceof ModelException modelException) {
                CUtil.broadcastMessageToGroupPlayer(
                        new SendMessage(
                                Component.translatable(
                                        "neuracraft.agent.chat.error.modelResponse",
                                        modelException.getMessage()
                                ),
                                chatMessage.env(),
                                chatMessage.player()
                        ),
                        agent.getPlayers().stream().toList()
                );
            }
            log.error("Agent message processing failed for {}", agent.getUUID(), e);
        } finally {
            agent.releaseMessageLock();
        }
    }
    
    // 这应该是一个一异步方法....
    // 调用这个方法的人应该确保自己获取了对应agent的锁....
    private @NotNull AgentResponse mcpCall(@NotNull Agent agent, @NotNull AgentResponse startResponse, Consumer<Component> print) {
        log.debug("AgentController MCP call start");
        AgentResponse agentResponse = startResponse;
        do {
            if (agentResponse.hasToolCalls()) {
                List<AgentMessage.ToolResult> results = new ArrayList<>();
                for (var toolCall : agentResponse.toolCalls()) {
                    results.add(new AgentMessage.ToolResult(
                            toolCall.id(),
                            mcpController.processToolCall(toolCall, print)
                    ));
                }
                log.debug("tool results from MCPController are: {}", results);
                agentResponse = agent.sendMessage(
                        AgentMessage.toolResults(results, agentResponse.player())
                );
            } else {
                List<Content> response = mcpController.processAgentInput(agentResponse.msg(), print);
                log.debug("response from MCPController is: {}", response);
                agentResponse = agent.sendMessage(new AgentMessage(response, agentResponse.player()));
            }
            log.debug("response from agent is: {}", agentResponse);
        } while (agentResponse.state() == AgentResponse.State.MCP_CALLING || agentResponse.state() == AgentResponse.State.START_MCP_CALL);
        log.debug("AgentController MCP call end and return {}", agentResponse);
        return agentResponse;
    }
    
    public static @NotNull Path getAgentPath(@NotNull Agent agent) {
        Path path = FileUtil.getAgentBaseUrl().resolve(agent.getModelName()).resolve(agent.getUUID() + "." + agent.getSuffix());
        log.debug("getAgentPath for {} is {}", agent, path);
        return path;
    }
    
    private synchronized void loadAllAgentFromFile() {
        if (!agentManager.getAllAgents().isEmpty()) {
            log.debug("Agents already loaded, skipping duplicate load");
            return;
        }
        List<Path> paths;
        try {
            // 获取所有文件
            paths = FileUtil.readAllFilePath(FileUtil.getAgentBaseUrl())
                            .stream()
                            .filter(path -> ! path.startsWith(FileUtil.agent_config_url))
                            .toList();
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Path path : paths) {
            if (path.toFile().isFile()) {
                log.debug("load agent from {}", path);
                // 让Agent自己判断文件是否是自己的
                String agentName = agentManager.getAgentByConfigFilePath(path);
                if (agentName == null) {
                    // 找不到模型
                    log.warn("can not find agentName model for {}", path);
                    continue;
                }
                log.debug("load agent name {}", agentName);
                // 创建聊天室并加载
                agentManager.createAgent(agentName).loadFromFile(path);
            }
        }
    }
    
    private void saveAllAgentToFile() {
        for (Agent agent : agentManager.getAllAgents()) {
            agent.saveToFile(
                    // path/to/world/agent/<modelName>/<uuid>.<suffix>
                    getAgentPath(agent)
            );
        }
    }

    public synchronized void start() {
        if (executor.isShutdown() || executor.isTerminated()) {
            executor = createExecutor();
        }
        acceptingMessages = true;
    }

    public synchronized void stop() {
        acceptingMessages = false;
        executor.shutdown();
        try {
            if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                return; // Normal termination
            }
        } catch (InterruptedException e) {
            log.warn("await is interrupt");
        }
        executor.shutdownNow();
    }

    private static @NotNull ExecutorService createExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
