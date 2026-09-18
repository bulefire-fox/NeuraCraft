# Skills

The NeoForge build recursively loads `SKILL.md` files from
`config/neuracraft/skills/`. A skill directory may contain reference material, but
only a file named `SKILL.md` is registered.

Each file starts with front matter:

```markdown
---
name: minecraft-weather
description: Query and explain Minecraft weather
triggers: [weather, rain]
always: false
---
# Weather

Use the weather tool for live data, then answer concisely.
```

`name` and `description` are required and names must be unique. `triggers` and
`keywords` are optional and are combined for relevance matching. `always: true`
injects the skill for every model request. The Markdown body is the instruction
context sent to the model.

The index is loaded at server startup and refreshed automatically before a request
when skill files are added or changed. A malformed file is isolated from valid
skills. The selected skills are injected into the system context of DeepSeek and GPT.
Silver Shadow keeps its original `systemPrompt` and does not receive skill injection.
Tool-result turns keep the skill selection from the original user request.
