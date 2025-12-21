package com.bulefire.neuracraft.core;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import com.bulefire.neuracraft.core.agent.entity.AgentMessage;
import com.bulefire.neuracraft.core.util.AgentOutOfTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ToString
@Log4j2
public abstract class AbsAgent implements Agent{
    // 聊天室名称
    @Getter
    @Setter
    private String name;
    // 聊天室uuid
    private final UUID uuid;
    // 玩家列表
    @Getter
    private final List<APlayer> players;
    // 管理员列表
    @Getter
    private final List<APlayer> admins;
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
    private final Timer timer;

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

    @Override
    public boolean hasAdmin(@NotNull APlayer player) {
        return this.admins.contains(player) || this.persistentAdmins.contains(player.name());
    }

    @Override
    public void removeAdmin(@NotNull APlayer player) {
        this.admins.remove(player);
    }

    @Override
    public @NotNull String sendMessage(@NotNull AgentMessage msg){
        if (timer.isOutOfTimes()){
            throw new AgentOutOfTime("out of times", timePerMin);
        }
        //String message = msg.toFormatedMessage();
        return message(msg.msg());
    }

    protected abstract @NotNull String message(@NotNull String msg);

    public @NotNull UUID getUUID() {
        return uuid;
    }

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

    protected static Logger getLogger(Class<?> klass) {
        return LogManager.getLogger(klass);
    }
}
