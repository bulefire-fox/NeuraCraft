package com.bulefire.neuracraft.core.mcp.mssage;

import com.bulefire.neuracraft.compatibility.entity.Content;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public non-sealed class MCPResponse implements MCPMessage {
    private String id;
    private Result result;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Result {
        private List<Content> content;
        private boolean isError;
        
        public Result appendTextFirst(@NotNull String append) {
            content.set(0, Content.of("text", append+content.get(0).textOrThrow()));
            return this;
        }
        
        public static @NotNull Result of(@NotNull MCPError error) {
            return Result.builder()
                    .isError(true)
                    .content(List.of(Content.of("text", error.getErrorMessage())))
                    .build();
        }
        
        public static @NotNull Result of(@NotNull String message) {
            return Result.builder()
                         .isError(false)
                         .content(List.of(Content.of("text", message)))
                         .build();
        }
    }
}
