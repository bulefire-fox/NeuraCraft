package com.bulefire.neuracraft.core.config;

import com.bulefire.neuracraft.core.agent.AgentController;
import lombok.extern.log4j.Log4j2;

import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * 监控 neuracraft.json 变化并在运行时热重载配置与提示词.
 */
@Log4j2
public final class ConfigFileWatcher {
    private static volatile boolean running = false;
    private static Thread watcherThread;
    private static final AtomicLong lastReload = new AtomicLong(0);
    private static final long DEBOUNCE_MS = 2000;

    private ConfigFileWatcher() {}

    public static synchronized void start() {
        if (running) return;
        running = true;
        watcherThread = new Thread(ConfigFileWatcher::watchLoop, "NCConfigWatcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
        log.info("ConfigFileWatcher started on {}", NCMainConfig.mainConfigPath);
    }

    public static synchronized void stop() {
        running = false;
        if (watcherThread != null) {
            watcherThread.interrupt();
            watcherThread = null;
        }
        log.info("ConfigFileWatcher stopped");
    }

    private static void watchLoop() {
        Path dir = NCMainConfig.mainConfigPath.getParent();
        if (dir == null) {
            log.error("Cannot watch config: parent dir is null for {}", NCMainConfig.mainConfigPath);
            return;
        }
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            dir.register(ws, ENTRY_CREATE, ENTRY_MODIFY);
            while (running) {
                WatchKey key;
                try {
                    key = ws.poll(2, java.util.concurrent.TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    if (changed != null && changed.toString().equals(NCMainConfig.mainConfigPath.getFileName().toString())) {
                        long now = System.currentTimeMillis();
                        if (now - lastReload.get() > DEBOUNCE_MS) {
                            lastReload.set(now);
                            reloadConfig();
                        }
                    }
                }
                if (!key.reset()) break;
            }
        } catch (Exception e) {
            log.error("ConfigFileWatcher error", e);
        }
    }

    private static void reloadConfig() {
        try {
            log.info("Config file changed, reloading...");
            NCMainConfig.load();
            AgentController.refreshPrompts();
            log.info("Config hot-reload complete");
        } catch (Exception e) {
            log.error("Config hot-reload failed", e);
        }
    }
}
