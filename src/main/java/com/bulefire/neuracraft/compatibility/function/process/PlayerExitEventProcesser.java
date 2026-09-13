package com.bulefire.neuracraft.compatibility.function.process;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Log4j2
public class PlayerExitEventProcesser {
    private static final Set<Consumer<ExitMessage>> messageFun = new HashSet<>();

    public static void registerFun(Consumer<ExitMessage> fun) {
        log.debug("register fun {} to chatEvent", fun);
        messageFun.add(fun);
    }

    public static void onPlayerExit(ExitMessage exitMessage) {
        log.debug("enter in onPlayerExit");
        log.debug(messageFun);
        for (Consumer<ExitMessage> fun : messageFun) {
            fun.accept(exitMessage);
        }
    }

    public record ExitMessage(APlayer player, ChatEventProcesser.ChatMessage.Env env) {

    }
}
