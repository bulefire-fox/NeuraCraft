package com.bulefire.neuracraft.core.mcp.mssage;

import com.google.gson.JsonElement;
import lombok.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public non-sealed class MCPResponse implements MCPMessage {
    @Builder.Default
    private String jsonrpc = "2.0";
    private String id;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Success extends MCPResponse {
        private Object result;
        
        public Success(@NotNull MCPResponse father, @NotNull Object result) {
            super(father.jsonrpc, father.id);
            this.result = result;
        }
        
        @Contract(" -> new")
        public static @NotNull SuccessBuilder successBuilder() {
            return new SuccessBuilder();
        }
        
        public static class SuccessBuilder extends MCPResponse.MCPResponseBuilder {
            private Object result;
            public SuccessBuilder() {
                super();
            }
            
            @Override
            public SuccessBuilder jsonrpc(final String jsonrpc) {
                super.jsonrpc(jsonrpc);
                return this;
            }
            
            @Override
            public SuccessBuilder id(final String id) {
                super.id(id);
                return this;
            }
            
            public SuccessBuilder result(final Object result) {
                this.result = result;
                return this;
            }
            
            @Override
            public Success build() {
                return new Success(super.build(), this.result);
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class Failed extends MCPResponse {
        private MCPError error;
        
        public Failed(@NotNull MCPResponse father, @NotNull MCPError error) {
            super(father.jsonrpc, father.id);
            this.error = error;
        }
        
        @Contract(" -> new")
        public static @NotNull FailedBuilder failedBuilder() {
            return new FailedBuilder();
        }
        
        public static class FailedBuilder extends MCPResponse.MCPResponseBuilder {
            private MCPError error;
            
            public FailedBuilder() {
                super();
            }
            
            @Override
            public FailedBuilder jsonrpc(final String jsonrpc) {
                super.jsonrpc(jsonrpc);
                return this;
            }
            
            @Override
            public FailedBuilder id(final String id) {
                super.id(id);
                return this;
            }
            
            public FailedBuilder error(final MCPError error) {
                this.error = error;
                return this;
            }
            
            @Override
            public Failed build() {
                return new Failed(super.build(), this.error);
            }
        }
    }
}
