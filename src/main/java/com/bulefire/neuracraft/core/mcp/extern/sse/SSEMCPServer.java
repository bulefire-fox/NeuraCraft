package com.bulefire.neuracraft.core.mcp.extern.sse;

import com.bulefire.neuracraft.core.mcp.extern.RemoteMCPServer;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

@Log4j2
public class SSEMCPServer implements RemoteMCPServer {
    private final String name;
    private final URL baseURL;
    
    public SSEMCPServer(@NotNull String name, @NotNull String url) throws MalformedURLException {
        this.name = name;
        if (!url.endsWith("/sse")) throw new IllegalArgumentException("url must end with /sse");
        this.baseURL = new URL(url.substring(0, url.length() - "/sse".length()));
    }
    
    @Override
    public void start() {
    
    }
    
    @Override
    public void stop() {
    
    }
    
    @Override
    public String send(String msg) {
        return "";
    }
}
