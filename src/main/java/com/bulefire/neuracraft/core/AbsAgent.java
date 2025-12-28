package com.bulefire.neuracraft.core;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.compatibility.util.CUtil;
import com.bulefire.neuracraft.compatibility.util.FileUtil;
import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.agent.AgentManager;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.util.AgentOutOfTime;
import com.google.gson.Gson;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@link Agent}的骨干实现，可以继承此类并进一步实现 <b>抽象方法</b> 和 <b>protected方法</b>
 * @author bulefire_fox
 * @since 2.0
 * @see Agent
 * @see AgentSerializationData
 * @see APlayer
 * @see AgentMessage
 * @see Timer
 * @implNote 建议仔细阅读源码&文档并合理使用默认实现和 <b>protected</b> 辅助方法， <b>protected</b> 方法建议重写为 <b>private</b>
 */
@ToString
@Log4j2
public abstract class AbsAgent implements Agent{
    // 聊天室名称
    @Getter
    @Setter
    private String name;
    // 聊天室 uuid
    private final UUID uuid;
    // 玩家列表
    @Getter
    private final List<APlayer> players;
    // 管理员列表
    @Getter
    private final List<APlayer> admins;
    // 持久化管理员
    @Getter
    private final List<String> persistentAdmins;
    // 模型名称
    @Getter
    private final String modelName;
    // 显示名称
    @Getter
    private final String disPlayName;
    // 每分钟消息次数
    private final int timePerMin;
    // 计时器
    // 此字段不会被序列化
    private final transient Timer timer;

    /**
     * 创建一个抽象地聊天室
     * persistentAdmins 由admins生成 <br/>
     * <pre>{@code this.persistentAdmins = admins.stream().map(APlayer::name).toList();}</pre>
     * @param name 聊天室名称
     * @param uuid 聊天室 uuid
     * @param players 玩家列表
     * @param admins 管理员列表
     * @param modelName 模型名称
     * @param disPlayName 显示名称
     * @param timePerMin 每分钟消息次数
     * @since 1.0
     */
    public AbsAgent(String name, UUID uuid, List<APlayer> players, @NotNull List<APlayer> admins, String modelName, String disPlayName, int timePerMin) {
        this.name = name;
        this.uuid = uuid;
        this.players = players;
        this.admins = admins;
        this.persistentAdmins = admins.stream().map(APlayer::name).toList();
        this.modelName = modelName;
        this.disPlayName = disPlayName;
        this.timePerMin = timePerMin;
        this.timer = new Timer(timePerMin);
    }

    /**
     * 创建一个抽象地聊天室,为抽象类的序列化辅助方法提供支持。<br>
     *
     * 由于采用final封装, 导致{@link Gson}无法反序列化到此类, 为此提供辅助方法
     * {@link AbsAgent#loadFileToManager(Path, Class, Class)}
     * 为子类提供序列化支持. 此构造函数为{@link AbsAgent#loadFileToManager(Path, Class, Class)}提供支持,
     * 由反序列化的数据创建。<br>
     *
     * 此构造器只能由子类或辅助方法调用，避免破坏封装
     * @param data 聊天室的序列化数据
     * @see AbsAgent#loadFileToManager(Path, Class, Class)
     * @see AgentSerializationData
     * @since 2.0
     */
    protected AbsAgent(@NotNull AgentSerializationData data) {
        this.name = data.name;
        this.uuid = data.uuid;
        this.players = data.players;
        this.admins = data.admins;
        this.persistentAdmins = data.persistentAdmins;
        this.modelName = data.modelName;
        this.disPlayName = data.disPlayName;
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
    }

    /**
     * 判断玩家是否为管理员
     * @param player 玩家
     * @return 是否是管理员
     * @apiNote 此方法在玩家不在 {@code admins} 列表时会去 {@code persistentAdmins} 中寻找是否存在指定玩家名
     * 如果存在则返回 {@code true}
     * @see AbsAgent#admins
     * @see AbsAgent#persistentAdmins
     */
    @Override
    public boolean hasAdmin(@NotNull APlayer player) {
        return this.admins.contains(player) || this.persistentAdmins.contains(player.name());
    }

    /**
     * 移除管理员
     * @param player 管理员
     * @apiNote 此方法不会移除 {@code persistentAdmins} 中的管理员
     */
    @Override
    public void removeAdmin(@NotNull APlayer player) {
        this.admins.remove(player);
    }

    /**
     * 发送消息, 默认进行频率限制
     * @param msg 传入的消息
     * @return 返回的消息
     * @throws AgentOutOfTime 当发送频率太快时抛出
     * @apiNote 此方法会调用抽象方法 {@link AbsAgent#message(String)}, 将传入的消息转为格式化后的消息并返回<br>
     * {@link AbsAgent#message(String)}由子类实现
     * @see AbsAgent#message(String)
     */
    @Override
    public @NotNull String sendMessage(@NotNull AgentMessage msg) throws AgentOutOfTime {
        if (timer.isOutOfTimes()){
            throw new AgentOutOfTime("out of times", timePerMin);
        }
        //String message = msg.toFormatedMessage();
        return message(msg.msg());
    }

    /**
     * 由子类实现的具体消息方法, 为 {@link AbsAgent#sendMessage(AgentMessage)} 提供支持
     * @param msg 格式化消息
     * @return 返回消息
     * @implSpec 此方法必须返回原始消息，格式化由框架处理, 传入的 {@code msg} 为格式化后的扁平消息，无需再次格式化.
     * @implNote 可以使用{@link CUtil#AiPOST(String, String, String)} 将消息发送至ai平台
     * @see AbsAgent#sendMessage(AgentMessage)
     * @see CUtil#AiPOST(String, String, String)
     */
    protected abstract @NotNull String message(@NotNull String msg);

    public @NotNull UUID getUUID() {
        return uuid;
    }

    /**
     * 获取日志记录器, 建议使用此方法获取日志记录器， 并接入 {@code modloader} 的日志管理 <br>
     * 使用方法:
     * <pre>{@code private static final Logger log = getLogger(DeepSeek.class);}</pre>
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
     * @see AbsAgent#sendMessage(AgentMessage)
     */
    protected static class Timer {
        private final int timesPerMin;
        private int times = 0;

        private static final ScheduledExecutorService t = Executors.newScheduledThreadPool(16);

        public Timer(int timesPerMin) {
            this.timesPerMin = timesPerMin;
            t.scheduleAtFixedRate(this::reset, 0, 60, TimeUnit.SECONDS);
        }

        public void add() {
            times++;
        }

        public void reset() {
            times = 0;
        }

        public boolean isOutOfTimes() {
            return times >= timesPerMin;
        }
    }

    /**
     * 为子类的 {@link AbsAgent#loadFromFile(Path)} 方法提供支持<br>
     * 用于加载文件到新的 {@code Agent} 并替换 {@link AgentManager} 中的实例
     * @param path 配置文件路径
     * @param classOfData {@code 数据类} 的 {@link Class} 对象
     * @param classOfAgent {@code Agent} 的 {@link Class} 对象
     * @param <D> 数据类类型
     * @param <A> Agent 类型
     * @since 2.0
     * @see AgentManager
     * @see AgentSerializationData
     * @see AbsAgent#loadFromFile(Path)
     * @apiNote 此方法会将新的 {@code Agent} 对象放入{@link AgentManager} 中,并删除旧的 {@code Agent} 对象
     */
    @SneakyThrows
    protected <D extends AgentSerializationData, A extends Agent> void loadFileToManager(@NotNull Path path, @NotNull Class<D> classOfData, @NotNull Class<A> classOfAgent) {
        // 泛型啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊
        D data = FileUtil.loadJsonFromFile(path, classOfData);
        // 报错怎么办?
        // 完蛋
        A agent = classOfAgent.getConstructor(classOfData).newInstance(data);
        // 固定 NPC
        var agentManager = AgentController.getAgentManager();
        // 替换
        agentManager.addAgent(agent);
        agentManager.removeAgent(this.getUUID());
    }

    /**
     * 数据类，为反序列化提供支持 <br>
     * 子类数据类需要继承此类
     * @since 2.0
     * @see AbsAgent#loadFileToManager(Path, Class, Class)
     */
    @Data
    protected static class AgentSerializationData {
        // 聊天室名称
        public String name;
        // 聊天室uuid
        public UUID uuid;
        // 玩家列表
        public List<APlayer> players;
        // 管理员列表
        public List<APlayer> admins;
        public List<String> persistentAdmins;
        // 模型名称
        public String modelName;
        // 显示名称
        public String disPlayName;
        // 每分钟消息次数
        public int timePerMin;

        @Contract(pure = true)
        public AgentSerializationData(@NotNull AbsAgent agent) {
            this.name = agent.name;
            this.uuid = agent.uuid;
            this.players = agent.players;
            this.admins = agent.admins;
            this.persistentAdmins = agent.persistentAdmins;
            this.modelName = agent.modelName;
            this.disPlayName = agent.disPlayName;
            this.timePerMin = agent.timePerMin;
        }

        public AgentSerializationData() {
            this.name = "";
            this.uuid = new UUID(0,0);
            this.players = new ArrayList<>(0);
            this.admins = new ArrayList<>(0);
            this.persistentAdmins = new ArrayList<>(0);
            this.modelName = "";
            this.disPlayName = "";
            this.timePerMin = -1;
        }
    }
}
