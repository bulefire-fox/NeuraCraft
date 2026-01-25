package com.bulefire.neuracraft.compatibility.function.process;

import com.bulefire.neuracraft.compatibility.entity.APlayer;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Log4j2
public class ChatEventProcesser {
    private static final Set<Consumer<ChatMessage>> messageFun = new HashSet<>();

    public static void registerFun(Consumer<ChatMessage> fun) {
        log.debug("register fun {} to chatEvent", fun);
        messageFun.add(fun);
    }

    public static void onChat(ChatMessage chatMessage) {
        log.debug("enter in onChat");
        log.debug(messageFun);
        for (Consumer<ChatMessage> fun : messageFun) {
            fun.accept(chatMessage);
        }
    }

    public record ChatMessage(String msg, APlayer player, Env env) {
        public enum Env {
            SERVER,
            CLIENT,
            SINGLE,
        }
    }
}
