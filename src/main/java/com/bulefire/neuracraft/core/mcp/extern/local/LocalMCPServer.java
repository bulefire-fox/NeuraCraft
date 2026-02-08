package com.bulefire.neuracraft.core.mcp.extern.local;

import com.bulefire.neuracraft.core.agent.AgentController;
import com.bulefire.neuracraft.core.mcp.MCPTool;
import com.bulefire.neuracraft.core.mcp.extern.AbsRemoteMCPServer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 表示本地 MCP server
 */
@Log4j2
public class LocalMCPServer extends AbsRemoteMCPServer {
    private final String name;
    private final String command;
    private final List<String> args;
    @Getter
    private Map<String, MCPTool> tools;
    private Process process;
    private BufferedReader reader;
    private BlockingDeque<String> readerQueue;
    private BufferedReader errorReader;
    private BlockingDeque<String> errorReaderQueue;
    private PrintWriter writer;
    private Thread monitor;
    private ExecutorService executor;
    
    public LocalMCPServer(@NotNull String name, @NotNull String command, @NotNull List<String> args) {
        this.name = name;
        this.command = command;
        this.args = args;
    }
    
    /**
     * 启动 MCP server
     */
    @Override
    @SneakyThrows
    public void start() {
        log.info("Starting local mcp server");
        // 构造启动命令
        args.add(0, command);
        log.debug("command: {}", args);
        ProcessBuilder pb = new ProcessBuilder(args);
        // 重定向错误流单独处理
        pb.redirectError(ProcessBuilder.Redirect.PIPE);
        process = pb.start();
        // 等待进程启动
        while (! process.isAlive()) {
            log.warn("MCP process {} is not alive, waiting...", name);
            process.wait(500);
        }
        // 接管stdio
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        writer = new PrintWriter(process.getOutputStream(), true);
        // 监视器
        monitor = new Thread(() -> {
            try {
                if (monitor.isInterrupted())
                    return;
                int exitCode = process.waitFor();
                log.error("MCP process exited with code: {}", exitCode);
                AgentController.gameLogger.accept(
                        Component.literal("MCP process %s exited with code: %s".formatted(name, exitCode))
                );
                // TODO: 处理错误 重启?
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        monitor.setName("stdio MCP server "+name+" monitor");
        monitor.setDaemon(true);
        monitor.start();
        // 用线程池管理stdio线程
        executor = Executors.newFixedThreadPool(2);
        readerQueue = new LinkedBlockingDeque<>();
        errorReaderQueue = new LinkedBlockingDeque<>();
        // 标准输出
        executor.execute(() -> {
            try {
                // 轮询并等待线程终止
                while (!Thread.currentThread().isInterrupted()) {
                    // MCP SDK 以行为单位输出
                    String line = reader.readLine();
                    log.debug("MCP process {} reader output: {}", name, line);
                    // 使用队列存储
                    if (line == null) continue;
                    readerQueue.put(line);
                }
            } catch (IOException | InterruptedException e) {
                AgentController.gameLogger.accept(
                        Component.literal("MCP process %s reader throw a Exception %s".formatted(name, e.getMessage()))
                );
                throw new RuntimeException(e);
            }
        });
        // 错误输出
        executor.execute(() -> {
            try {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                    sb.append(line);
                }
                log.debug("MCP process {} error output: {}", name, sb.toString());
                errorReaderQueue.put(sb.toString());
            } catch (IOException | InterruptedException e) {
                AgentController.gameLogger.accept(
                        Component.literal("MCP process %s reader throw a Exception %s".formatted(name, e.getMessage()))
                );
                throw new RuntimeException(e);
            }
        });
        
        // 查看是否有启动输出
        // 例如启动命令有误等
        String line = readerQueue.poll(1, TimeUnit.SECONDS);
        if (line != null)
            log.debug("MCP process start output: {}", line);
        line = errorReaderQueue.poll(1, TimeUnit.SECONDS);
        if (line != null)
            log.debug("MCP process start error output: {}", line);
        // 我们不处理
        log.debug("local mcp server process {} pid {}", process, process.pid());
        init();
        registerAllTool(tools);
    }
    
    /**
     * 停止 本地 MCP server
     */
    @Override
    @SneakyThrows
    public void stop() {
        log.info("Stopping local mcp server");
        
        monitor.interrupt();
        
        process.destroy();
        
        // 3. 关闭线程池
        executor.shutdown();
        executor.shutdownNow();
        
        // 4. 安全关闭流
        try {
            if (reader != null) {
                reader.close();
                log.debug("Reader closed successfully");
            }
        } catch (IOException e) {
            log.warn("Failed to close reader", e);
        }
        
        if (writer != null) {
            writer.close();
            log.debug("Writer closed successfully");
        }
        
        try {
            if (errorReader != null) {
                errorReader.close();
                log.debug("ErrorReader closed successfully");
            }
        } catch (IOException e) {
            log.warn("Failed to close errorReader", e);
        }
        
        log.info("Local MCP server stopped");
    }
    
    /**
     * 发送 jsonrpc 消息
     * @param json jsonrpc字符串
     * @return 返回的结果
     */
    @Override
    @SneakyThrows
    public @NotNull String send(@NotNull String json) {
        // 检查进程是否存活
        if (! process.isAlive())
            throw new RuntimeException("Process is not alive");
        log.debug("Send: {}", json.replace("\n", "").replace(" ", ""));
        // 发送消息, 以行为单位
        writer.println(json.replace("\n", "").replace(" ", ""));
        // 刷新缓存
        writer.flush();
        
        // 构造输出流返回
        StringBuilder rsb = new StringBuilder();
        // 先读取一行, 若还有数据则继续读取
        do {
            // 5s超时
            String line = readerQueue.poll(5, TimeUnit.SECONDS);
            if (line != null)
                rsb.append(line);
            else
                // 读取到null则退出循环
                break;
            // 在绝大多数场景下这个do-while循环只会执行一次, 因为标准MCP SDK以行为单位返回数据.
            // 所以队列里应该只存在一条消息并\n结尾
        } while (! readerQueue.isEmpty());
        // 构造错误流返回
        StringBuilder esb = new StringBuilder();
        do {
            String line = errorReaderQueue.poll(5, TimeUnit.SECONDS);
            if (line != null)
                esb.append(line);
            else
                break;
        } while (! errorReaderQueue.isEmpty());
        
        // 有回复
        if (! rsb.isEmpty()) {
            log.debug("Receive: {}", rsb.toString());
            log.debug("Error: {}", esb.toString());
            // 忽略错误流的返回,因为MCP SDK的debug内容会输出在错误流中
            return rsb.toString();
        }
        // 错误回复
        if (! esb.isEmpty()) {
            // 这时才真正有错误信息
            log.error("Error: {}", esb.toString());
            throw new RuntimeException("Error: \n" + esb);
        }
        // 超时
        throw new RuntimeException("Timeout");
    }
    
    private @Nullable String readLineWithTimeout(long timeoutMs) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Future<String> future = executor.submit(() -> {
            try {
                return reader.readLine();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.warn("Read line timed out after {} ms", timeoutMs);
            return null;
        } finally {
            executor.shutdown();
        }
    }
}
