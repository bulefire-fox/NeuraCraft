package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.entity.Content;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.agent.entity.AgentResponse;
import com.bulefire.neuracraft.core.inside.model.openai.OpenAiToolCall;
import com.bulefire.neuracraft.core.util.AgentOutOfTime;
import com.bulefire.neuracraft.core.util.UnSupportFormattedMessage;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Agent}的骨干实现，可以继承此类并进一步实现 <b>抽象方法</b> 和 <b>protected方法</b>
 *
 * @author bulefire_fox
 * @implNote 建议仔细阅读源码&文档并合理使用默认实现和 <b>protected</b> 辅助方法， <b>protected</b> 方法建议重写为 <b>private</b>
 * @see Agent
 * @see AgentSerializationData
 * @see APlayer
 * @see AgentMessage
 * @see Timer
 * @since 2.0
 */
@ToString
@Log4j2
public abstract class AbsAgent implements Agent {
    // 聊天室名称
    @Getter
    @Setter
    private String name;
    // 聊天室 uuid
    private final UUID uuid;
    // 玩家列表
    @Getter
    private final Set<APlayer> players;
    // 管理员列表
    @Getter
    private final Set<APlayer> admins;
    // 持久化管理员
    @Getter
    private final Set<UUID> persistentAdmins;
    // 模型名称
    @Getter
    private final String modelName;
    // 显示名称
    @Getter
    private final String disPlayName;
    // 后缀
    @Getter
    private final String suffix;
    // 每分钟消息次数
    private final int timePerMin;
    // 计时器
    // 此字段不会被序列化
    private final transient Timer timer;
    // 消息状态机: IDLE → MESSAGING → (MCP_CALLING) → IDLE
    private enum MessageState { IDLE, MESSAGING, MCP_CALLING }
    // Runtime-only state; never persist locks or the current message cycle.
    private final transient AtomicReference<MessageState> messageState = new AtomicReference<>(MessageState.IDLE);
    private final transient ReentrantLock sendLock = new ReentrantLock();
    private transient List<OpenAiToolCall> lastToolCalls = List.of();
    
    /**
     * 创建一个抽象地聊天室
     * persistentAdmins 由admins生成 <br/>
     * <pre>{@code this.persistentAdmins = admins.stream().map(APlayer::name).toList();}</pre>
     *
     * @param name        聊天室名称
     * @param uuid        聊天室 uuid
     * @param players     玩家列表
     * @param admins      管理员列表
     * @param modelName   模型名称
     * @param disPlayName 显示名称
     * @param timePerMin  每分钟消息次数
     * @since 1.0
     */
    public AbsAgent(String name, UUID uuid, Set<APlayer> players, @NotNull Set<APlayer> admins, String modelName, String disPlayName, String suffix, int timePerMin) {
        this.name = name;
        this.uuid = uuid;
        this.players = Sets.newConcurrentHashSet(players);
        this.admins = Sets.newConcurrentHashSet(admins);
        this.persistentAdmins = Sets.newConcurrentHashSet(admins.stream().map(APlayer::uuid).toList());
        this.modelName = modelName;
        this.disPlayName = disPlayName;
        this.suffix = suffix;
        this.timePerMin = timePerMin;
        this.timer = new Timer(timePerMin);
    }


    protected AbsAgent(@NotNull AgentSerializationData data) {
        this.name = data.name;
        this.uuid = data.uuid;
        this.players = data.players;
        this.admins = data.admins;
        this.persistentAdmins = data.persistentAdmins;
        this.modelName = data.modelName;
        this.disPlayName = data.disPlayName;
        this.suffix = data.suffix;
        this.timePerMin = data.timePerMin;
        this.timer = new Timer(timePerMin);
    }
    
    @Override
    public void addPlayer(@NotNull APlayer player) {
        this.players.add(player);
    }
    
    @Override
    public void removePlayer(@NotNull APlayer player) {
        this.players.remove(player);
    }
    
    @Override
    public void addAdmin(@NotNull APlayer player) {
        this.admins.add(player);
        this.persistentAdmins.add(player.uuid());
    }
    
    /**
     * 判断玩家是否为管理员
     *
     * @param player 玩家
     * @return 是否是管理员
     * @apiNote 此方法在玩家不在 {@code admins} 列表时会去 {@code persistentAdmins} 中寻找是否存在指定玩家名
     *         如果存在则返回 {@code true}
     * @see AbsAgent#admins
     * @see AbsAgent#persistentAdmins
     */
    @Override
    public boolean hasAdmin(@NotNull APlayer player) {
        return this.admins.contains(player) || this.persistentAdmins.contains(player.uuid());
    }
    
    /**
     * 移除管理员
     *
     * @param player 管理员
     * @apiNote 此方法不会移除 {@code persistentAdmins} 中的管理员
     */
    @Override
    public void removeAdmin(@NotNull APlayer player) {
        this.admins.remove(player);
    }
    
    @Override
    public boolean isMCPCalling() {
        return messageState.get() == MessageState.MCP_CALLING;
    }

    @Override
    public boolean isMessaging() {
        return messageState.get() != MessageState.IDLE;
    }

    @Override
    public void lockMessageLock() {
        sendLock.lock();
    }

    @Override
    public void releaseMessageLock() {
        if (sendLock.isHeldByCurrentThread())
            sendLock.unlock();
    }

    @Override
    public void releaseAllMessageLock() {
        while (sendLock.isHeldByCurrentThread()) {
            sendLock.unlock();
        }
    }

    @Override
    public void abortMessage() {
        messageState.set(MessageState.IDLE);
    }

    /**
     * 发送消息 — 频率限制 + MCP调用状态机.
     * 每次调用都获取自己拥有的锁计数，结束时只释放当前调用获取的那一层。
     */
    @Override
    public @NotNull AgentResponse sendMessage(@NotNull AgentMessage msg) throws AgentOutOfTime, UnSupportFormattedMessage {
        sendLock.lock();
        boolean keepMcpState = false;
        try {
            messageState.set(MessageState.MESSAGING);
            if (timer.isOutOfTimes()) {
                throw new AgentOutOfTime("out of times", timePerMin);
            }
            lastToolCalls = List.of();
            // message 方法可以通过 setToolCalls 设置工具调用
            String rawResponse = message(msg);
            // tool call
            if (!lastToolCalls.isEmpty()) {
                MessageState prev = messageState.getAndSet(MessageState.MCP_CALLING);
                keepMcpState = true;
                AgentResponse.State s = (prev == MessageState.MCP_CALLING)
                        ? AgentResponse.State.MCP_CALLING
                        : AgentResponse.State.START_MCP_CALL;
                return new AgentResponse(rawResponse, s, msg.player(), lastToolCalls);
            }
            // 如果message没有处理tool call则在此兜底
            if (isMCPCall(rawResponse)) {
                MessageState prev = messageState.getAndSet(MessageState.MCP_CALLING);
                keepMcpState = true;
                AgentResponse.State s = (prev == MessageState.MCP_CALLING)
                        ? AgentResponse.State.MCP_CALLING
                        : AgentResponse.State.START_MCP_CALL;
                return new AgentResponse(rawResponse, s, msg.player());
            }

            // 非MCP响应
            if (messageState.getAndSet(MessageState.IDLE) == MessageState.MCP_CALLING) {
                return new AgentResponse(rawResponse, AgentResponse.State.FINISH_MCP_CALL, msg.player());
            }
            timer.add();
            return new AgentResponse(rawResponse, AgentResponse.State.NORMAL, msg.player());
        } finally {
            if (!keepMcpState)
                messageState.set(MessageState.IDLE);
            releaseMessageLock();
        }
    }
    
    /**
     * 判断是否为MCP工具调用. 仅当响应是严格的 {@code {"tool_call":{"id":"...","parameters":{...}}}} 结构时返回true.
     * 不再将任意JSON误判为MCP调用.
     */
    protected static boolean isMCPCall(String jsonString) {
        if (jsonString == null || jsonString.isEmpty() || jsonString.trim().isEmpty())
            return false;
        try {
            JsonElement root = JsonParser.parseString(jsonString);
            if (!root.isJsonObject()) return false;
            var obj = root.getAsJsonObject();
            if (!obj.has("tool_call")) return false;
            var toolCall = obj.get("tool_call");
            if (!toolCall.isJsonObject()) return false;
            var tc = toolCall.getAsJsonObject();
            return tc.has("id") && tc.has("parameters") && tc.get("parameters").isJsonObject();
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
    
    /**
     * 由子类实现的具体消息方法, 为 {@link AbsAgent#sendMessage(AgentMessage)} 提供支持
     *
     * @param messages 格式化消息
     * @return 返回消息
     * @implSpec 此方法必须返回原始消息，格式化由框架处理, 传入的 {@code messages} 为格式化后的扁平消息，无需再次格式化.
     *         {@code messages.get(0)} 为用户输入的消息.其他为环境信息等由框架生成的消息. 如使用 {@code Open AI} 格式的 api 可以附加. 也可以直接忽略.
     * @implNote 可以使用{@link CUtil#AiPOST(String, String, String)} 将消息发送至ai平台. 可以自行处理工具调用，并将调用的工具通过AbsAgent#setToolCalls(List)
     * 告知框架。
     * @see AbsAgent#sendMessage(AgentMessage)
     * @see CUtil#AiPOST(String, String, String)
     * @see AbsAgent#setToolCalls(List)
     */
    protected @NotNull String message(@NotNull List<Content> messages) throws UnSupportFormattedMessage {
        throw new UnsupportedOperationException("This agent does not implement the list-based message entry point");
    }

    /**
     * Structured message entry point. Existing agents overriding the list-based
     * method remain source-compatible, while OpenAI agents can preserve tool roles.
     */
    protected @NotNull String message(@NotNull AgentMessage message) throws UnSupportFormattedMessage {
        return message(message.getFormattedMessages());
    }

    protected final void setToolCalls(@NotNull List<OpenAiToolCall> toolCalls) {
        lastToolCalls = List.copyOf(toolCalls);
    }
    
    public @NotNull UUID getUUID() {
        return uuid;
    }
    
    /**
     * 获取日志记录器, 建议使用此方法获取日志记录器， 并接入 {@code modloader} 的日志管理 <br>
     * 使用方法:
     * <pre>{@code private static final Logger log = getLogger(DeepSeek.class);}</pre>
     *
     * @param klass 类
     * @return 日志记录器
     * @apiNote 默认返回 {@link Logger} 的实例,接入 {@code modloader} 的日志管理.
     * @see Logger
     */
    protected static Logger getLogger(@NotNull Class<?> klass) {
        return LogManager.getLogger(klass);
    }
    
    /**
     * 计时器，用于限制发送频率.<br>
     * 为 {@link AbsAgent#sendMessage(AgentMessage)} 提供支持
     *
     * @see AbsAgent#sendMessage(AgentMessage)
     */
    protected static class Timer {
        private final int timesPerMin;
        private final AtomicInteger times = new AtomicInteger(0);
        
        private static final ScheduledExecutorService t = Executors.newScheduledThreadPool(1);
        
        public Timer(int timesPerMin) {
            this.timesPerMin = timesPerMin;
            t.scheduleAtFixedRate(this::reset, 0, 60, TimeUnit.SECONDS);
        }
        
        public void add() {
            times.getAndIncrement();
        }
        
        public void reset() {
            times.set(0);
        }
        
        public boolean isOutOfTimes() {
            return times.get() >= timesPerMin;
        }
    }
    
    /**
     * 为子类的 {@link AbsAgent#loadFromFile(Path)} 方法提供支持<br>
     * 用于加载文件到新的 {@code Agent} 并替换 {@link AgentManager} 中的实例
     *
     * @param path         配置文件路径
     * @param classOfData  {@code 数据类} 的 {@link Class} 对象
     * @param classOfAgent {@code Agent} 的 {@link Class} 对象
     * @param <D>          数据类类型
     * @param <A>          Agent 类型
     * @apiNote 此方法会将新的 {@code Agent} 对象放入{@link AgentManager} 中,并删除旧的 {@code Agent} 对象
     * @see AgentManager
     * @see AgentSerializationData
     * @see AbsAgent#loadFromFile(Path)
     * @since 2.0
     */
    @SneakyThrows
    protected <D extends AgentSerializationData, A extends Agent> void loadFileToManager(@NotNull Path path, @NotNull Class<D> classOfData, @NotNull Class<A> classOfAgent) {
        // 泛型啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊
        D data = FileUtil.loadJsonFromFile(path, classOfData);
        // 报错怎么办?
        // 完蛋
        Constructor<A> constructor = classOfAgent.getDeclaredConstructor(classOfData);
        constructor.setAccessible(true);
        A agent = constructor.newInstance(data);
        // 固定 NPC
        var agentManager = AgentController.getInstance().getAgentManager();
        // 替换
        agentManager.addAgent(agent);
        agentManager.removeAgent(this.getUUID());
    }
    
    /**
     * 数据类，为反序列化提供支持 <br>
     * 子类数据类需要继承此类
     *
     * @see AbsAgent#loadFileToManager(Path, Class, Class)
     * @since 2.0
     */
    @Data
    protected static class AgentSerializationData {
        // 聊天室名称
        public String name;
        // 聊天室uuid
        public UUID uuid;
        // 玩家列表
        public Set<APlayer> players;
        // 管理员列表
        public Set<APlayer> admins;
        public Set<UUID> persistentAdmins;
        // 模型名称
        public String modelName;
        // 显示名称
        public String disPlayName;
        // 后缀
        public String suffix;
        // 每分钟消息次数
        public int timePerMin;
        
        @Contract(pure=true)
        public AgentSerializationData(@NotNull AbsAgent agent) {
            this.name = agent.name;
            this.uuid = agent.uuid;
            this.players = agent.players;
            this.admins = agent.admins;
            this.persistentAdmins = agent.persistentAdmins;
            this.modelName = agent.modelName;
            this.disPlayName = agent.disPlayName;
            this.suffix = agent.suffix;
            this.timePerMin = agent.timePerMin;
        }
        
        public AgentSerializationData() {
            this.name = "";
            this.uuid = new UUID(0, 0);
            this.players = Sets.newConcurrentHashSet();
            this.admins = Sets.newConcurrentHashSet();
            this.persistentAdmins = Sets.newConcurrentHashSet();
            this.modelName = "";
            this.disPlayName = "";
            this.suffix = "";
            this.timePerMin = - 1;
        }
    }
}
