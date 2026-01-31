package com.bulefire.neuracraft.core.mcp.mssage;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public sealed interface MCPMessage permits MCPNotification, MCPRequest, MCPResponse {
    Gson gobalGson = new Gson();
    
    // region is
    static boolean isNotification(@NotNull MCPMessage msg) {
        return msg instanceof MCPNotification;
    }
    
    static boolean isRequest(@NotNull MCPMessage msg) {
        return msg instanceof MCPRequest;
    }
    
    static boolean isResponse(@NotNull MCPMessage msg) {
        return msg instanceof MCPResponse;
    }
    
    static boolean isResponseSuccess(@NotNull MCPMessage msg) {
        return msg instanceof MCPResponse.Success;
    }
    
    static boolean isResponseFailed(@NotNull MCPMessage msg) {
        return msg instanceof MCPResponse.Failed;
    }
    
    // endregion
    // region as
    static @NotNull MCPRequest asRequest(@NotNull MCPMessage msg) {
        if (! isRequest(msg))
            throw new ClassCastException("MCPMessage is not a request");
        return Objects.requireNonNull((MCPRequest) msg);
    }
    
    static @NotNull MCPResponse asResponse(@NotNull MCPMessage msg) {
        if (! isResponse(msg))
            throw new ClassCastException("MCPMessage is not a response");
        return Objects.requireNonNull((MCPResponse) msg);
    }
    
    static @NotNull MCPResponse.Success asResponseSuccess(@NotNull MCPMessage msg) {
        if (! isResponseSuccess(msg))
            throw new ClassCastException("MCPMessage is not a response success");
        return Objects.requireNonNull((MCPResponse.Success) msg);
    }
    
    static @NotNull MCPResponse.Failed asResponseFailed(@NotNull MCPMessage msg) {
        if (! isResponseFailed(msg))
            throw new ClassCastException("MCPMessage is not a response failed");
        return Objects.requireNonNull((MCPResponse.Failed) msg);
    }
    
    static @NotNull MCPNotification asNotification(@NotNull MCPMessage msg) {
        if (! isNotification(msg))
            throw new ClassCastException("MCPMessage is not a notification");
        return Objects.requireNonNull((MCPNotification) msg);
    }
    
    // endregion
    // region builder
    static @NotNull MCPRequest.MCPRequestBuilder requestBuilder() {
        return MCPRequest.builder();
    }
    
    static @NotNull MCPResponse.MCPResponseBuilder responseBuilder() {
        return MCPResponse.builder();
    }
    
    static @NotNull MCPResponse.Success.SuccessBuilder responseSuccessBuilder() {
        return MCPResponse.Success.successBuilder();
    }
    
    static @NotNull MCPResponse.Failed.FailedBuilder responseFailedBuilder() {
        return MCPResponse.Failed.failedBuilder();
    }
    
    static @NotNull MCPNotification.MCPNotificationBuilder notificationBuilder() {
        return MCPNotification.builder();
    }
    
    // endregion
    // region json
    static @NotNull MCPRequest toRequest(@NotNull String json) {
        return gobalGson.fromJson(json, MCPRequest.class);
    }
    
    static @NotNull MCPResponse toResponse(@NotNull String json) {
        return gobalGson.fromJson(json, MCPResponse.class);
    }
    
    static @NotNull MCPResponse.Success toResponseSuccess(@NotNull String json) {
        return gobalGson.fromJson(json, MCPResponse.Success.class);
    }
    
    static @NotNull MCPResponse.Failed toResponseFailed(@NotNull String json) {
        return gobalGson.fromJson(json, MCPResponse.Failed.class);
    }
    
    static @NotNull MCPNotification toNotification(@NotNull String json) {
        return gobalGson.fromJson(json, MCPNotification.class);
    }
    
    static @NotNull String toJson(@NotNull MCPMessage msg) {
        return gobalGson.toJson(msg);
    }
    // endregion
}
