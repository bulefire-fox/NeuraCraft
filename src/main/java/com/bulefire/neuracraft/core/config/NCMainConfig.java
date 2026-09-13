package com.bulefire.neuracraft.core.config;

import com.bulefire.neuracraft.compatibility.util.FileUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class NCMainConfig {
    public static final Path mainConfigPath = FileUtil.base_url.resolve("neuracraft.json");
    public static final File mainConfigFile = mainConfigPath.toFile();

    public static void init() {
        if (!mainConfigFile.exists()) {
            try {
                mainConfigFile.getParentFile().mkdirs();
                mainConfigFile.createNewFile();
                NeuraCraft nc = new NeuraCraft();
                nc.setPrefix("AI");
                nc.setThreadsNumber(20);
                nc.setPrompts(new Prompts());
                FileUtil.saveJsonToFile(nc, mainConfigPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            load();
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Unable to load main configuration", e);
        }
    }

    private static final AtomicReference<ConfigSnapshot> CURRENT =
            new AtomicReference<>(new ConfigSnapshot("AI", 20, new Prompts()));

    public static String getPrefix() {
        return CURRENT.get().prefix();
    }

    public static int getThreadsNumber() {
        return CURRENT.get().threadsNumber();
    }

    public static Prompts getPrompts() {
        return CURRENT.get().prompts();
    }

    public static void load() throws IOException {
        NeuraCraft nc = FileUtil.loadJsonFromFile(mainConfigPath, NeuraCraft.class);
        if (nc == null)
            throw new IOException("main config is empty");
        String nextPrefix = nc.getPrefix();
        int nextThreads = nc.getThreadsNumber();
        if (nextPrefix == null || nextPrefix.isBlank())
            throw new IllegalArgumentException("prefix must not be blank");
        if (nextThreads <= 0)
            throw new IllegalArgumentException("threadsNumber must be greater than zero");
        Prompts nextPrompts = nc.getPrompts() == null ? new Prompts() : nc.getPrompts();
        CURRENT.set(new ConfigSnapshot(nextPrefix, nextThreads, nextPrompts));
        log.info("NCMainConfig loaded: prefix={}, threads={}", nextPrefix, nextThreads);
    }

    private record ConfigSnapshot(String prefix, int threadsNumber, Prompts prompts) {
    }

    @Getter
    @Setter
    public static class NeuraCraft {
        private String prefix;
        private int threadsNumber;
        private Prompts prompts;
    }

    @Getter
    @Setter
    public static class Prompts {
        private String agentIdentity;
        private String aliveMcpHead;
        private String mcpFormat;
        private String mcpOutput;
        private String mcpWorkflow;
        private String reiteratedKey;

        // -- fallback accessors: never return null --

        public String getAgentIdentity() {
            return emptyToDefault(agentIdentity, DEFAULT_AGENT_IDENTITY);
        }

        public String getAliveMcpHead() {
            return emptyToDefault(aliveMcpHead, DEFAULT_ALIVE_MCP_HEAD);
        }

        public String getMcpFormat() {
            return emptyToDefault(mcpFormat, DEFAULT_MCP_FORMAT);
        }

        public String getMcpOutput() {
            return emptyToDefault(mcpOutput, DEFAULT_MCP_OUTPUT);
        }

        public String getMcpWorkflow() {
            return emptyToDefault(mcpWorkflow, DEFAULT_MCP_WORKFLOW);
        }

        public String getReiteratedKey() {
            return emptyToDefault(reiteratedKey, DEFAULT_REITERATED_KEY);
        }

        private static String emptyToDefault(String val, String def) {
            return (val == null || val.isBlank()) ? def : val;
        }

        // ---- built-in defaults ----

        public static final String DEFAULT_AGENT_IDENTITY = """
                你的身份与核心原则
                1.  你可以主动使用我提供的工具来获取实时数据、执行操作或处理信息。
                2.  当用户问题直接涉及或隐含需要以下工具所提供的能力时，你必须主动调用工具。调用前无需额外征求我的许可。
                3.  你需要对工具调用的时机、参数的正确性和最终回答的完整性负责。
                """;

        public static final String DEFAULT_ALIVE_MCP_HEAD = """
                可用工具由客户端通过 OpenAI Chat Completions API 的 tools 字段提供。
                如果请求中没有工具定义，则不要自行编造工具。
                """;

        public static final String DEFAULT_MCP_FORMAT = """
                工具调用遵循 OpenAI 原生格式。工具名称、描述和 JSON Schema
                由 tools 字段提供，不要从普通文本中猜测参数结构。
                """;

        public static final String DEFAULT_MCP_OUTPUT = """
                需要工具时，使用 assistant 消息中的原生 tool_calls 字段，
                不要在 content 中输出自定义 JSON 工具调用。
                """;

        public static final String DEFAULT_MCP_WORKFLOW = """
                工具调用流程：
                1. 需要工具时返回 assistant.tool_calls。
                2. 客户端执行每个调用，并以 role=tool、tool_call_id 和 content
                   将结果追加到下一次请求。
                3. 收到工具结果后，直接生成对用户原问题的最终回答。
                """;

        public static final String DEFAULT_REITERATED_KEY = """
                关键指令重申：
                - 不要把工具调用写入普通 content。
                - 严格使用 tools 字段提供的函数和参数 Schema。
                - 工具结果已经通过 role=tool 提供，整合后直接回答用户。
                """;
    }
}
