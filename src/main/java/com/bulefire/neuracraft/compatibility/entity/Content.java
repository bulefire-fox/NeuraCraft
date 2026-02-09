package com.bulefire.neuracraft.compatibility.entity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record Content(@NotNull String type, @NotNull Object text) {
    @Contract("_, _ -> new")
    public static @NotNull Content of(@NotNull String type, @NotNull Object text) {
        return new Content(type, text);
    }
    
    @Contract(pure=true)
    public static @NotNull Content ifTypeOrThrow(@NotNull Content content, @NotNull String type){
         if (content.type.equals(type)) {
             return content;
         }
         throw new IllegalArgumentException("Content is not text.");
    }
    
    @Contract(pure=true)
    public @NotNull String textOrThrow(){
        return (String) ifTypeOrThrow(this, "text").text;
    }
}