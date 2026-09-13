# Repository Guidelines

## Project Structure & Module Organization

NeuraCraft-main is the active NeoForge 26.1.2 mod, built with Gradle and Java 25.
The sibling `NeuraCraft-forge-1.20.1/` directory is a separate legacy Forge build
and must be maintained with its own Java 17 toolchain.

- `src/main/java/com/bulefire/neuracraft/`: production code. `core/` contains the framework, agents, MCP integration, configuration, plugins, and commands; `compatibility/` adapts Minecraft/NeoForge events and entities; `mod/` contains mod-facing registration and listeners.
- `src/main/resources/`: Forge metadata, mixin configuration, translations, models, and textures. Keep assets under `assets/neuracraft/`.
- `src/test/java/`: JUnit and executable integration-style test clients.
- `src/test/mcp_server/`: small Python MCP server fixtures and examples.
- `resource/`: documentation and project images. `libs/`: locally supplied dependency jars.
- `build/`: generated outputs; do not edit or commit them.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper:

```text
.\gradlew.bat build       # compile, test, package, and create the jar artifact
.\gradlew.bat test        # run JUnit Platform tests
.\gradlew.bat runClient   # launch a NeoForge development client
.\gradlew.bat runServer   # launch a NeoForge development server
.\gradlew.bat clean       # remove generated build outputs
```

Use Java 25 for this checkout; Gradle’s toolchain is configured for it. The first build may download Minecraft, NeoForge, and Maven dependencies.

Runtime configuration is stored under `config/neuracraft/`. Agent archives are stored
under the active world’s `agent/` directory. External MCP transports are `stdio`,
SSE, and Streamable HTTP. The external adapter uses `mcp-java-sdk 2.0.1`
(`mcp`, `mcp-core`, and `mcp-json-jackson2`); keep SDK-specific types inside
`core/mcp/extern/`. Internal calls use immutable `MCPRequest` and `MCPResponse`
objects, `ToolId` for identity, and `MCPController.callTool(toolName, params)`
for ordinary programmatic calls. Built-in agents currently include GPT, DeepSeek,
and YinYing; DouBao is not registered until its implementation is complete.

## Coding Style & Naming Conventions

Use 4-space indentation, UTF-8, and standard Java conventions: `PascalCase` for classes, `camelCase` for methods and fields, and lowercase package names. Keep packages aligned with the existing `com.bulefire.neuracraft` hierarchy. Match nearby code, use focused comments for non-obvious Forge lifecycle or MCP behavior, and keep translations/assets synchronized in both `en_us.json` and `zh_cn.json` when applicable. No formatter or linter is configured, so review changed files before submitting.

MCP tool registration must go through `MCPManager`; tool IDs are unique and
display names are descriptive indexes only. Keep prompt generation deterministic
and preserve the original remote server name, tool name, and raw JSON Schema in
`MCPToolInfo`. Do not add reflection against private SDK internals; configure
transport-specific JSON mappers at the transport boundary.

## Testing Guidelines

Tests use JUnit Jupiter through `useJUnitPlatform()`. Name unit tests after the behavior they verify, and keep external-service or Minecraft-dependent checks clearly separated from fast tests. Run `.\gradlew.bat test`; for MCP changes, also exercise the relevant Java client or Python server fixture under `src/test/mcp_server/`.

## Commit & Pull Request Guidelines

History uses short, lowercase summaries, but contributors should prefer a concise imperative subject, such as `fix MCP tool timeout`. PRs should explain the behavior change, list validation commands, link related issues, and include screenshots or logs for in-game, command, or UI changes. Call out config, dependency, mapping, or resource changes explicitly.

## Security & Configuration

Never commit API tokens, server credentials, or machine-specific paths. Review configuration and generated jars before committing, especially changes involving external AI providers or MCP servers. Plugin jars may use the explicit `META-INF/neuracraft-plugin.json` descriptor with API version `1`; legacy annotation scanning remains compatibility-only.
