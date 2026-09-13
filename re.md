# NeuraCraft 重构执行方案

## 1. 目标与边界

本方案针对当前 NeoForge 26.1.2、Java 25 版本的 NeuraCraft。

目标：

1. 先修复运行时正确性问题，避免 Agent、MCP 和服务器生命周期互相影响。
2. 保持现有用户配置、Agent 存档和插件使用方式尽可能兼容。
3. 降低模型适配、配置加载、事件桥接和插件系统之间的耦合。
4. 建立可以持续验证的单元测试和运行时测试。

本阶段不建议：

- 整体重写 Agent 系统。
- 同时迁移 Minecraft/NeoForge 版本。
- 立即拆成多个 Gradle 子项目。
- 在没有测试的情况下更换 MCP SDK。

执行原则：每个阶段都保持项目可以编译；每个阶段结束后都运行测试；涉及存档格式的改动必须提供兼容读取或迁移逻辑。

## 2. 当前基线

项目主要结构：

```text
src/main/java/com/bulefire/neuracraft/
├── core/           Agent、MCP、模型、配置、插件和命令
├── compatibility/ Minecraft 无关的兼容桥接和工具
└── mod/            NeoForge 注册、事件监听和配置
```

当前验证基线：

```text
./gradlew.bat clean test
./gradlew.bat build
```

两项命令当前均可成功完成。

当前测试不足：主要只有 MCP 输入 JSON 解析测试，尚未覆盖 Agent 生命周期、事件、文件存档、插件加载、网络超时和外部 MCP。

## 3. 阶段一：建立安全基线

### 目标

先固定当前行为，增加能够暴露关键问题的测试，避免后续重构无法判断是否引入回归。

### 任务

1. 新增测试目录结构：

```text
src/test/java/com/bulefire/neuracraft/
├── core/agent/
├── core/mcp/
├── core/config/
├── compatibility/
└── testutil/
```

2. 为 `AgentManager` 增加测试：

- 创建 Agent。
- 按 UUID 查询和删除。
- 按名称查询。
- 配置文件后缀识别。
- 重复 UUID 的覆盖行为。

3. 为 `MCPManager` 增加测试：

- 注册工具。
- 重复名称处理。
- 按工具名删除时不能误删同 display name 的其他工具。
- 按 display name 删除。
- 非法工具拒绝注册。

4. 为配置加载增加测试：

- 缺少配置文件时创建默认配置。
- 缺少 Prompt 字段时使用默认值。
- 非法 JSON 不应悄悄覆盖现有有效配置。

5. 在测试工具中增加一个假的 `AbsAgent` 和假的 `MCPTool`，避免测试依赖真实网络或 Minecraft 服务。

### 验收标准

- `./gradlew.bat clean test` 成功。
- 新增测试不依赖外部 API、游戏客户端或本机 Python 环境。
- 每个后续阶段至少能复用一组回归测试。

## 4. 阶段二：修复 P0 运行时问题

### 4.1 修复未入群玩家的 Agent 映射

涉及：

- `core/agent/PlayerManager.java`
- `core/agent/AgentController.java`

执行：

1. 删除 `PlayerManager.emptyUUID` 的业务语义。
2. 未入群玩家不写入 Agent 映射，或使用明确的 `Optional<UUID>` 查询结果。
3. `AgentController.AsyncMessage()` 获取不到 Agent 时直接返回并发送提示。
4. 玩家加入/退出消息只在玩家确实属于 Agent 时发送给对应 Agent。

验收：

- 新玩家进入服务器不会产生空指针。
- 玩家退出时不会向不存在的 Agent 发送消息。
- 陈旧映射不会导致消息线程崩溃。

### 4.2 统一锁释放

涉及：

- `core/agent/AgentController.java`
- `core/agent/AbsAgent.java`

执行：

1. 将 Agent 消息处理主体抽成 `processMessage()`。
2. 所有 `lockMessageLock()` 都使用 `try/finally`。
3. MCP 调用、保存存档、广播消息抛出异常时仍然释放锁。
4. `AbsAgent.sendMessage()` 不再通过 `sendLock.isLocked()` 判断是否需要加锁。
5. 明确锁的所有权：只有当前线程持有锁时才允许释放。

验收：

- 普通消息异常后仍可继续发送下一条消息。
- MCP 调用异常后仍可继续发送下一条消息。
- 并发向同一 Agent 发送消息时不会交叉修改历史。

### 4.3 修复外部 MCP 响应

涉及：

- `core/mcp/MCPController.java`
- `core/mcp/extern/AbsRemoteMCPServer.java`
- `core/mcp/mssage/MCPResponse.java`

执行：

1. 外部工具响应必须复制 `request.getId()`。
2. `processAgentInput()` 对空 JSON、缺少 `tool_call`、缺少 ID 和缺少参数进行结构化错误处理。
3. 找不到工具时返回 MCP 错误，不使用 `Objects.requireNonNull()` 触发空指针。
4. 外部工具返回空 content 时生成可读的错误信息。

验收：

- 外部 MCP 工具成功调用后能进入 Agent 的下一轮消息。
- 不存在的工具不会使消息线程异常退出。
- 非法模型输出会转化为用户可见的失败结果。

### 4.4 修复外部 MCP Schema 转换

涉及：

- `core/mcp/extern/AbsRemoteMCPServer.java`

执行：

1. `properties` 缺失时使用空 Map。
2. `required` 缺失时使用空 List。
3. 参数描述缺失时使用参数名或空描述。
4. 不把所有复杂 JSON Schema 强行映射为简单 Java 类型。
5. 在 `MCPToolInfo` 中保留原始 Schema，调用时直接传递参数 Map。

验收：

- 无参数工具可以注册。
- 只有可选参数的工具可以注册。
- 数组、对象、枚举参数不会在注册阶段崩溃。

## 5. 阶段三：整理服务器和世界生命周期

### 目标

将“服务器启动/停止”和“世界或维度加载/卸载”分离。

### 任务

1. `AgentController`：

- Agent 文件只在服务器启动后加载一次。
- 服务器停止时保存一次。
- 不再在每次 `LevelEvent.Load` 中重复加载全部 Agent。

2. `ExternMCPController`：

- 外部 MCP 服务在服务器启动时启动。
- 外部 MCP 服务在服务器停止时关闭。
- 删除对 `LevelUnloadEventProcess` 的关闭注册。
- 防止 `init()` 重复创建线程池和重复注册关闭回调。

3. 增加显式生命周期接口：

```java
interface Lifecycle {
    void start();
    void stop();
}
```

4. 将初始化状态改为显式状态机：

```text
NEW -> STARTING -> RUNNING -> STOPPING -> STOPPED
```

### 验收标准

- 加载 Nether 或 End 不会停止 MCP 服务。
- 多次触发世界加载事件不会重复加载或替换正在使用的 Agent。
- 服务器正常关闭时，Agent 存档和 MCP 子进程都能关闭。
- 服务器重新启动后，外部 MCP 可以重新连接。

## 6. 阶段四：统一配置和持久化

### 6.1 配置快照

涉及：

- `core/config/NCMainConfig.java`
- `core/config/ConfigFileWatcher.java`
- `core/inside/model/*/*Config.java`

执行：

1. 每种配置解析为不可变 `record` 或不可变配置对象。
2. 使用 `AtomicReference<ConfigSnapshot>` 发布最新配置。
3. 配置加载分为：

```text
read -> parse -> validate -> publish
```

4. 解析或校验失败时保留旧配置，并记录明确错误。
5. 校验：

- URL 必须是合法 HTTP/HTTPS 地址。
- `timePerMin` 必须大于零。
- 模型名、显示名不能是空字符串。
- Token 不写入普通日志。

### 6.2 原子文件写入

涉及：

- `compatibility/util/FileUtil.java`

执行：

1. 写入临时文件。
2. `flush` 并关闭文件。
3. 使用原子移动替换目标文件。
4. 失败时保留旧文件。
5. 不再吞掉 `JsonIOException`。

### 6.3 配置热重载

执行：

1. WatchService 只负责检测变化。
2. 重载动作交给统一的 `ConfigService`。
3. 重载成功后显式通知模型和 Prompt 服务。
4. 明确哪些配置只影响新建 Agent，哪些配置立即影响已有 Agent。

验收标准：

- 配置文件被编辑到一半时不会破坏旧配置。
- 热重载失败不会让当前 Agent 失效。
- 已有 Agent 的 Prompt 更新行为有明确测试。

## 7. 阶段五：抽象模型客户端

### 目标

消除 `GPT`、`DeepSeek`、`YinYing` 中重复的配置、HTTP 和响应处理逻辑。

建议结构：

```text
core/model/
├── ModelClient.java
├── ModelConfig.java
├── ChatRequest.java
├── ChatResponse.java
├── ConversationStore.java
├── openai/OpenAiCompatibleClient.java
├── deepseek/DeepSeekClient.java
└── yinying/YinYingClient.java
```

建议接口：

```java
public interface ModelClient {
    ChatResponse send(ChatRequest request) throws ModelException;
}
```

执行顺序：

1. 先抽取 HTTP 客户端，不改变 Agent 对外接口。
2. 再抽取 OpenAI-compatible 请求和响应格式。
3. 将 DeepSeek 和 GPT 迁移到统一客户端。
4. 最后处理 YinYing 的专用字段。
5. DouBao 在实现完成前不要注册为可用 Agent。

### HTTP 客户端要求

- 设置连接超时和读取超时。
- 正确处理所有 2xx 响应。
- 正确读取错误响应。
- 始终关闭连接。
- 日志中隐藏 Token。
- 默认不记录完整 Prompt 和用户消息。
- 对网络错误、HTTP 错误、JSON 错误使用不同异常类型。

## 8. 阶段六：重构插件 API

### 当前问题

当前插件加载器通过扫描 JAR 内所有 class 并反射执行 `init()`，存在重复加载、资源未关闭、错误影响整个启动流程和类加载边界不清晰等问题。

### 建议结构

```text
core/plugin/
├── PluginDescriptor.java
├── PluginContext.java
├── PluginLifecycle.java
├── PluginRegistry.java
└── PluginLoader.java
```

建议插件生命周期：

```java
public interface PluginLifecycle {
    void initialize(PluginContext context);
    void shutdown();
}
```

执行：

1. 插件 JAR 增加显式描述文件。
2. 描述文件声明插件主类、版本和 API 兼容版本。
3. 使用 `ServiceLoader` 或明确入口类，减少全 JAR 扫描。
4. 每个插件独立捕获初始化异常。
5. 插件成功加载后才加入已加载列表。
6. 关闭时关闭 `JarFile` 和 `URLClassLoader`。
7. 子 Mod 加载不能重复执行已经加载的插件。

兼容策略：

- 第一版保留旧注解扫描作为兼容模式。
- 新插件优先使用显式描述文件。
- 后续版本再移除旧扫描机制。

## 9. 阶段七：整理 MCP 内部模型

### 目标

让内部 MCP 工具、插件 MCP 工具和外部 MCP 工具共享同一套调用协议。

建议：

1. 将 `MCPRequest`、`MCPResponse` 改为不可变对象。
2. 用统一的 `ToolId` 表示工具唯一 ID。
3. `MCPToolInfo` 保存：

- 工具来源。
- 服务器名。
- 原始工具名。
- 原始 JSON Schema。
- 显示名称和描述。

4. `MCPManager` 使用一个主 Map 管理工具，再按来源建立索引。
5. 删除工具时只删除指定工具，不删除同组工具。
6. Prompt 生成改为结构化 JSON 或稳定文本模板，避免依赖 Map 遍历顺序。

## 10. 阶段八：更新文档和工程配置

必须同步检查：

- `AGENTS.md`
- `README.md`
- `resource/docs/README_ZH.md`
- 内置模型配置指南
- `src/main/resources/META-INF/neoforge.mods.toml`
- `src/main/resources/neuracraft.mixins.json`

统一内容：

- NeoForge 26.1.2。
- Java 25。
- Gradle Wrapper 使用方式。
- 配置文件实际路径。
- 当前可用模型。
- DouBao 是否已完成。
- 外部 MCP 支持的 transport 类型。
- 插件 API 和兼容版本。

## 11. 推荐提交顺序

如果以后恢复 Git 管理，建议按以下逻辑拆分提交：

1. `test establish runtime regression baseline`
2. `fix empty agent and message lock lifecycle`
3. `fix external mcp request response handling`
4. `separate server and level lifecycle`
5. `harden config and atomic persistence`
6. `extract model client abstraction`
7. `introduce explicit plugin lifecycle`
8. `update documentation for neoforge`

每次提交都应满足：

```text
./gradlew.bat clean test
./gradlew.bat build
```

涉及游戏事件或 MCP 的提交，还应增加：

```text
./gradlew.bat runClient
./gradlew.bat runServer
```

并记录启动日志、服务器关闭日志和关键交互结果。

## 12. 最终完成标准

重构完成后应满足：

- 玩家未入群时不会触发 Agent 空指针。
- Agent 消息和 MCP 异常不会永久占用锁。
- 外部 MCP 工具能完成完整调用闭环。
- 无参数和复杂 Schema 工具可以正常注册。
- 世界切换不会停止外部 MCP。
- 配置热重载失败不会破坏旧配置。
- Token、Prompt 和玩家消息不会被无保护地写入日志。
- Agent 存档写入具备原子替换能力。
- 插件可以独立加载、失败和关闭。
- GPT、DeepSeek 等模型不再重复实现同一套 HTTP 基础设施。
- 测试覆盖 AgentManager、MCPManager、配置、存档和关键生命周期。
- `AGENTS.md`、README 和实际构建配置保持一致。

