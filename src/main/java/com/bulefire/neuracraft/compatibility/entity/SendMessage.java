package com.bulefire.neuracraft.compatibility.entity;

import com.bulefire.neuracraft.compatibility.function.process.ChatEventProcesser;
import net.minecraft.network.chat.Component;

public record SendMessage(Component message, ChatEventProcesser.ChatMessage.Env env, APlayer player) {
}
