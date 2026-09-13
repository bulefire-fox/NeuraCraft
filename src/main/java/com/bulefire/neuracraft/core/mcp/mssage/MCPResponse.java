package com.bulefire.neuracraft.core.mcp.mssage;

import com.bulefire.neuracraft.compatibility.entity.Content;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable internal MCP response.
 */
public non-sealed class MCPResponse implements MCPMessage {
    private final String id;
    private final Result result;

    public MCPResponse(String id, Result result) {
        this.id = id;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public Result getResult() {
        return result;
    }

    public MCPResponse withId(String newId) {
        return new MCPResponse(newId, result);
    }

    public MCPResponse withResult(Result newResult) {
        return new MCPResponse(id, newResult);
    }

    public static MCPResponseBuilder builder() {
        return new MCPResponseBuilder();
    }

    public static final class MCPResponseBuilder {
        private String id;
        private Result result;

        public MCPResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MCPResponseBuilder result(Result result) {
            this.result = result;
            return this;
        }

        public MCPResponse build() {
            return new MCPResponse(id, result);
        }
    }

    public static final class Result {
        private final List<Content> content;
        private final boolean isError;

        public Result(List<Content> content, boolean error) {
            this.content = content == null ? List.of() : List.copyOf(content);
            this.isError = error;
        }

        public List<Content> getContent() {
            return content;
        }

        public boolean isError() {
            return isError;
        }

        public @NotNull Result appendTextFirst(@NotNull String append) {
            if (content.isEmpty()) {
                return new Result(List.of(Content.of("text", append)), isError);
            }
            List<Content> updated = new ArrayList<>(content);
            updated.set(0, Content.of("text", append + content.get(0).textOrThrow()));
            return new Result(updated, isError);
        }

        public static @NotNull Result of(@NotNull MCPError error) {
            return new Result(List.of(Content.of("text", error.getErrorMessage())), true);
        }

        public static @NotNull Result of(@NotNull String message) {
            return new Result(List.of(Content.of("text", message)), false);
        }

        public static ResultBuilder builder() {
            return new ResultBuilder();
        }

        public static final class ResultBuilder {
            private List<Content> content = List.of();
            private boolean error;

            public ResultBuilder content(List<Content> content) {
                this.content = content;
                return this;
            }

            public ResultBuilder isError(boolean error) {
                this.error = error;
                return this;
            }

            public Result build() {
                return new Result(content, error);
            }
        }
    }
}
