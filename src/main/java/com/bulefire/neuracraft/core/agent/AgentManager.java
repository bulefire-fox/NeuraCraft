package com.bulefire.neuracraft.core.agent;

import com.bulefire.neuracraft.core.Agent;
import com.bulefire.neuracraft.core.util.NoAgentFound;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Log4j2
public class AgentManager {
    private final Map<UUID, Agent> agents;
    private final Map<String, Supplier<Agent>> agentMapping;
    private final List<Function<Path, String>> agentPathConsumer;

    public AgentManager(Map<UUID, Agent> agents, Map<String, Supplier<Agent>> agentSuppliers, List<Function<Path, String>> agentPathConsumer) {
        this.agents = agents;
        this.agentMapping = agentSuppliers;
        this.agentPathConsumer = agentPathConsumer;
    }

    public AgentManager() {
        this.agents = new HashMap<>();
        this.agentMapping = new HashMap<>();
        this.agentPathConsumer = new ArrayList<>();
    }

    public void registerAgentMapping(@NotNull String name, @NotNull Supplier<Agent> agentSupplier) {
        log.debug("register agent {}", name);
        agentMapping.put(name, agentSupplier);
    }

    public Agent getAgentMapping(@NotNull String name){
        return agentMapping.get(name).get();
    }

    public List<String> getAllAliveAgentKeys() {
        return new ArrayList<>(agentMapping.keySet());
    }

    public Agent removeAgentMapping(@NotNull UUID uuid) {
        log.debug("remove agent {}", uuid);
        return agents.remove(uuid);
    }

    public Agent creatAgent(@NotNull String agentName) throws NoAgentFound{
        var getter = agentMapping.get(agentName);
        if (getter == null)
            throw new NoAgentFound("can not find agentName model for " + agentName);
        Agent create = getter.get();
        if (create == null)
            throw new NoAgentFound("can not find agentName model for " + agentName);
        agents.put(create.getUUID(), create);
        return create;
    }

    public Agent addAgent(@NotNull Agent agent) {
        agents.put(agent.getUUID(), agent);
        return agent;
    }

    public Agent removeAgent(@NotNull UUID uuid) {
        return agents.remove(uuid);
    }

    public Agent getAgent(UUID uuid) {
        return agents.get(uuid);
    }

    public List<Agent> getAgentByName(String name) {
        List<Agent> result = new ArrayList<>();
        for (Agent agent : agents.values())
            if (agent.getName().equals(name))
                result.add(agent);
        return result;
    }

    public List<Agent> getAllAgents() {
        return List.copyOf(agents.values());
    }

    public void registerAgentPathConsumer(Function<Path, String> consumer){
        agentPathConsumer.add(consumer);
    }

    public void deleteAgentPathConsumer(Function<Path, String> consumer){
        agentPathConsumer.remove(consumer);
    }

    public String getAgentByConfigFilePath(Path path){
        for (Function<Path, String> consumer : agentPathConsumer) {
            String agent = consumer.apply(path);
            if(agent != null){
                return agent;
            }
        }
        return null;
    }
}
