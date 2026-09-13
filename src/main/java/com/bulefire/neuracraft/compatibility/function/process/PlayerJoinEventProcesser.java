package com.bulefire.neuracraft.compatibility.function.process;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Log4j2
public class PlayerJoinEventProcesser {
    private static final Set<Consumer<JoinMessage>> messageFun = new HashSet<>();

    public static void registerFun(Consumer<JoinMessage> fun) {
        log.debug("register fun {} to chatEvent", fun);
        messageFun.add(fun);
    }

    public static void onPlayerJoin(JoinMessage joinMessage) {
        log.debug("enter in onPlayerJoin");
        log.debug(messageFun);
        for (Consumer<JoinMessage> fun : messageFun) {
            fun.accept(joinMessage);
        }
    }

    public record JoinMessage(APlayer player, ChatEventProcesser.ChatMessage.Env env) {

    }
}
