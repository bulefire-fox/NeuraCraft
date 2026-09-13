package com.bulefire.neuracraft.core.config;

import java.net.URI;

public final class ConfigValidation {
    private ConfigValidation() {
    }

    public static void validateModel(String modelName, String displayName, int timePerMin, String url) {
        requireText(modelName, "modelName");
        requireText(displayName, "displayName");
        if (timePerMin <= 0)
            throw new IllegalArgumentException("timePerMin must be greater than zero");
        requireHttpUrl(url, "url");
    }

    public static void requireHttpUrl(String value, String field) {
        requireText(value, field);
        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(field + " must be a valid HTTP/HTTPS URL", e);
        }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
            throw new IllegalArgumentException(field + " must be a valid HTTP/HTTPS URL");
        if (uri.getHost() == null || uri.getHost().isBlank())
            throw new IllegalArgumentException(field + " must be a valid HTTP/HTTPS URL");
    }

    public static void requireText(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " must not be blank");
    }
}
