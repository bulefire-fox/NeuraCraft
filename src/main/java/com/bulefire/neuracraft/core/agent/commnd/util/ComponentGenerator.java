package com.bulefire.neuracraft.core.agent.commnd.util;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ComponentGenerator {
    public static @NotNull Component withHoverAndCopy(@NotNull String s, @NotNull String color) {
        Objects.requireNonNull(s);
        Objects.requireNonNull(color);
        return Component.literal(s)
                        .withStyle(style -> style
                                .withColor(TextColor.parseColor(color))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("neuracraft.command.hover.copy_to_clipboard")))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, s))
                        );
    }
}
