package com.bulefire.neuracraft.core.inside.model;

public interface ModelClient {
    ChatResponse send(ChatRequest request) throws ModelException;
}
