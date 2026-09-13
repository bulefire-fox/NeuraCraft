package com.bulefire.neuracraft.core.command.util;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ComponentGenerator {
    public static @NotNull Component withHoverAndCopy(@NotNull String s, @NotNull String color) {
        return withHoverAndCopy(s, s, color);
    }

    public static @NotNull Component withHoverAndCopy(
            @NotNull String displayText,
            @NotNull String clipboardText,
            @NotNull String color
    ) {
        Objects.requireNonNull(displayText);
        Objects.requireNonNull(clipboardText);
        Objects.requireNonNull(color);
        return Component.literal(displayText)
                        .withStyle(style -> style
                                .withColor(TextColor.parseColor(color).result().orElseThrow())
                                .withHoverEvent(new HoverEvent.ShowText(Component.translatable("neuracraft.agent.command.hover.copy_to_clipboard")))
                                .withClickEvent(new ClickEvent.CopyToClipboard(clipboardText))
                        );
    }
}
