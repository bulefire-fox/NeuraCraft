package com.bulefire.neuracraft.core.skill;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillManagerTest {
    @TempDir
    Path skillsRoot;

    @Test
    void reloadsNestedSkillsAndSelectsRelevantInstructions() throws Exception {
        writeSkill("weather/SKILL.md", """
                ---
                name: weather
                description: 查询 Minecraft 天气
                triggers: [天气, weather]
                ---
                调用天气工具，并解释晴天、下雨和雷暴。
                """);
        writeSkill("docs/SKILL.md", """
                ---
                name: docs
                description: 编写和审阅文档
                triggers: [文档, docs]
                ---
                遵循文档结构并检查事实。
                """);

        SkillManager manager = new SkillManager(skillsRoot);
        SkillManager.ReloadReport report = manager.reload();

        assertEquals(2, report.loaded());
        assertTrue(report.errors().isEmpty());
        assertEquals(1, manager.select("现在天气怎么样？").size());
        assertEquals("weather", manager.select("现在天气怎么样？").get(0).name());
        assertTrue(manager.buildContext("现在天气怎么样？").contains("调用天气工具"));
        assertFalse(manager.buildContext("完全无关的问题").contains("调用天气工具"));
    }

    @Test
    void alwaysSkillIsInjectedAndInvalidSkillDoesNotDiscardValidSkills() throws Exception {
        writeSkill("always/SKILL.md", """
                ---
                name: safety
                description: 基础安全约束
                always: true
                ---
                永远遵守安全边界。
                """);
        writeSkill("broken/SKILL.md", """
                ---
                name: broken
                ---
                no description
                """);

        SkillManager manager = new SkillManager(skillsRoot);
        SkillManager.ReloadReport report = manager.reload();

        assertEquals(1, report.loaded());
        assertEquals(1, report.errors().size());
        assertEquals("safety", manager.select("普通问题").get(0).name());
        assertTrue(manager.buildContext("普通问题").contains("永远遵守安全边界"));
    }

    @Test
    void discoversSkillAddedAfterInitialLoad() throws Exception {
        SkillManager manager = new SkillManager(skillsRoot);
        manager.reload();

        writeSkill("weather/SKILL.md", """
                ---
                name: weather
                description: 查询天气
                triggers: [天气]
                ---
                使用天气工具。
                """);

        assertTrue(manager.buildContext("天气怎么样").contains("使用天气工具"));
    }

    private void writeSkill(String relativePath, String content) throws Exception {
        Path file = skillsRoot.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }
}
