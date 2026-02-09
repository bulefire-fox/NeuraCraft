package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.core.util.NoAgentFound;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * AgentManager <br>
 * 用于管理 {@link Agent} 实例的创建和加载函数
 * 保存创建的 {@link Agent} 实例与 {@link UUID} 的映射关系
 *
 * @author bulefire_fox
 * @version 1.0
 * @see AgentController
 * @since 2.0
 */
@Log4j2
public class AgentManager {
    private final ConcurrentMap<UUID, Agent> agents;
    private final ConcurrentMap<String, Supplier<Agent>> agentMapping;
    private final Set<Function<Path, String>> agentPathConsumer;
    
    private final ReadWriteLock agentsLock = new ReentrantReadWriteLock();
    private final Lock agentsWriteLock = agentsLock.writeLock();
    private final Lock agentsReadLock = agentsLock.readLock();
    
    public AgentManager(ConcurrentMap<UUID, Agent> agents, ConcurrentMap<String, Supplier<Agent>> agentSuppliers, Set<Function<Path, String>> agentPathConsumer) {
        this.agents = agents;
        this.agentMapping = agentSuppliers;
        this.agentPathConsumer = Sets.newCopyOnWriteArraySet(agentPathConsumer);
    }
    
    public AgentManager() {
        this.agents = new ConcurrentHashMap<>();
        this.agentMapping = new ConcurrentHashMap<>();
        this.agentPathConsumer = Sets.newCopyOnWriteArraySet();
    }
    
    /**
     * 注册一个 {@link Agent} 的{@linkplain Supplier 创建函数}
     *
     * @param name          {@linkplain Supplier 创建函数}的名称
     * @param agentSupplier {@linkplain Supplier 创建函数}, 包装为 {@link Supplier}
     * @see AgentManager#agentMapping
     */
    public void registerAgentMapping(@NotNull String name, @NotNull Supplier<Agent> agentSupplier) {
        log.debug("register agent {}", name);
        agentMapping.put(name, agentSupplier);
    }
    
    /**
     * 获取一个 {@link Agent} 的实例
     *
     * @param name {@link Agent} 的注册名称
     * @return {@link Agent} 实例
     * @see AgentManager#agentMapping
     */
    public Agent getAgentMapping(@NotNull String name) {
        Supplier<Agent> supplier = agentMapping.get(name);
        return supplier != null ? supplier.get() : null;
    }
    
    /**
     * 获取所有已注册的 {@link Agent} 的名称
     *
     * @return 所有已注册的 {@link Agent} 的名称, 与源列表不是一个对象.
     * @see AgentManager#agentMapping
     */
    public List<String> getAllAliveAgentKeys() {
        return List.copyOf(agentMapping.keySet());
    }
    
    /**
     * 移除一个 {@link Agent} 的名字和对应的 {@linkplain Supplier 创建函数}
     *
     * @param name {@link Agent} 的注册名称
     * @see AgentManager#agents
     */
    public void removeAgentMapping(@NotNull String name) {
        log.debug("remove agentMapping {}", name);
        agentMapping.remove(name);
    }
    
    /**
     * 创建一个 {@link Agent} 实例
     *
     * @param agentName {@link Agent} 的注册名称
     * @return 创建的 {@link Agent} 实例
     * @throws NoAgentFound 如果没有找到对应的 {@link Agent} 注册名的 {@linkplain Supplier 创建函数}
     * @see AgentManager#agentMapping
     */
    public Agent createAgent(@NotNull String agentName) throws NoAgentFound {
        agentsWriteLock.lock();
        try {
            var getter = agentMapping.get(agentName);
            if (getter == null)
                throw new NoAgentFound("can not find agentName model for " + agentName);
            Agent create = getter.get();
            if (create == null)
                throw new NoAgentFound("can not find agentName model for " + agentName);
            agents.put(create.getUUID(), create);
            return create;
        } finally {
            agentsWriteLock.unlock();
        }
    }
    
    /**
     * 添加一个 {@link Agent} 实例
     *
     * @param agent 添加的 {@link Agent} 实例
     * @return 添加的 {@link Agent} 实例
     * @apiNote 重复添加行为与 {@link Map#put(Object, Object)}} 一致
     * @see AgentManager#agents
     */
    public Agent addAgent(@NotNull Agent agent) {
        agents.put(agent.getUUID(), agent);
        return agent;
    }
    
    /**
     * 移除一个 {@link Agent} 实例
     *
     * @param uuid 移除的 {@link Agent} 的 UUID
     * @return 移除的 {@link Agent} 实例
     * @see AgentManager#agents
     */
    public Agent removeAgent(@NotNull UUID uuid) {
        return agents.remove(uuid);
    }
    
    /**
     * 获取一个 {@link Agent} 实例
     *
     * @param uuid {@link Agent} 的 UUID
     * @return 获取的 {@link Agent} 实例
     * @see AgentManager#agents
     */
    public Agent getAgent(@NotNull UUID uuid) {
        return agents.get(uuid);
    }
    
    /**
     * 获取一个指定 {@linkplain Agent#getName() name} 的 {@link Agent} 实例的列表
     *
     * @param name {@link Agent} 的 {@linkplain Agent#getName() name}
     * @return 指定 {@linkplain Agent#getName() name} 的 {@link Agent} 列表
     * @see AgentManager#agents
     * @see Agent#getName()
     */
    public List<Agent> getAgentByName(String name) {
        agentsReadLock.lock();
        try {
            return agents.values().stream()
                         .filter(agent -> agent.getName().equals(name))
                         .toList();
        } finally {
            agentsReadLock.unlock();
        }
    }
    
    /**
     * 重新加载 指定 {@linkplain Agent#getName() name} 的 {@link Agent} (们)的配置文件,
     * 如果有多个符合指定 {@linkplain Agent#getName() name} 的 {@link Agent}, 则全部重新加载
     *
     * @param name {@link Agent} 的 {@linkplain Agent#getName() name}
     * @see AgentManager#getAgentByName(String)
     * @see Agent#reloadConfig()
     * @see AgentManager#agents
     */
    public void reloadAgentConfig(@NotNull String name) {
        agentsReadLock.lock();
        try {
            List<Agent> agentList = agents.values().stream()
                                          .filter(agent -> agent.getName().equals(name))
                                          .toList();
            if (agentList.isEmpty())
                throw new NoAgentFound("can not find agent for " + name);
            agentList.forEach(Agent::reloadConfig);
        } finally {
            agentsReadLock.unlock();
        }
    }
    
    /**
     * 重新加载一个 {@link Agent} 的配置文件, 通过 {@link UUID}
     *
     * @param uuid {@link Agent} 的 UUID
     * @see AgentManager#getAgent(UUID)
     * @see Agent#reloadConfig()
     * @see AgentManager#agents
     */
    public void reloadAgentConfig(@NotNull UUID uuid) {
        Agent agent = this.getAgent(uuid);
        if (agent != null)
            agent.reloadConfig();
        else
            throw new NoAgentFound("can not find agent for " + uuid);
    }
    
    /**
     * 重新加载所有 {@link Agent} 的配置文件
     *
     * @see Agent#reloadConfig()
     * @see AgentManager#agents
     */
    public void reloadAllAgentConfig() {
        agentsReadLock.lock();
        try {
            agents.values().forEach(Agent::reloadConfig);
        } finally {
            agentsReadLock.unlock();
        }
    }
    
    /**
     * 获取包含所有 {@link Agent} 的列表
     *
     * @return 包含所有 {@link Agent} 的列表
     * @apiNote 列表为与 {@linkplain AgentManager#agents 源列表} 不是一个对象
     * @see AgentManager#agents
     */
    public List<Agent> getAllAgents() {
        return List.copyOf(agents.values());
    }
    
    /**
     * 注册一个判断文件是否为 {@linkplain Agent 自己} 的文件的函数, 包装为 {@link Function}
     *
     * @param consumer 获取 {@linkplain Path 文件路径} 并返回 {@linkplain String 模型名称} 的函数, 如果不是 {@linkplain Agent 自己的模型} 则返回 {@code null}
     * @apiNote 函数返回 {@code null} 则表示不是 {@linkplain Agent 模型}
     * @see AgentManager#agentPathConsumer
     */
    public void registerAgentPathConsumer(Function<Path, String> consumer) {
        agentPathConsumer.add(consumer);
    }
    
    /**
     * 删除一个 {@linkplain Function 函数}
     *
     * @param consumer 同 {@link AgentManager#registerAgentPathConsumer(Function)} 中的 {@code consumer}
     * @see AgentManager#agentPathConsumer
     * @see AgentManager#registerAgentPathConsumer(Function)
     */
    public void deleteAgentPathConsumer(Function<Path, String> consumer) {
        agentPathConsumer.remove(consumer);
    }
    
    /**
     * 获取一个指定文件路径的 {@linkplain Agent 模型} {@linkplain String 名称}
     *
     * @param path 文件路径
     * @return 获取的 {@linkplain Agent 模型} 名称, 未找到则返回 {@code null}
     * @see AgentManager#agentPathConsumer
     * @see AgentManager#agentPathConsumer
     * @see AgentManager#registerAgentPathConsumer(Function)
     */
    public String getAgentByConfigFilePath(Path path) {
        for (Function<Path, String> consumer : agentPathConsumer) {
            String agent = consumer.apply(path);
            if (agent != null) {
                return agent;
            }
        }
        return null;
    }
}
