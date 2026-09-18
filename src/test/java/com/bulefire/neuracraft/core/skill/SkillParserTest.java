package com.bulefire.neuracraft.core.skill;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillParserTest {
    @Test
    void parsesSkillFrontMatterAndMarkdownInstructions() {
        Skill skill = SkillParser.parse("""
                ---
                name: minecraft-weather
                description: 查询和解释 Minecraft 天气
                triggers: [天气, weather, 下雨]
                always: true
                ---
                # Weather

                使用天气工具获取实时天气，再用简洁中文回答。
                """, Path.of("weather", "SKILL.md"));

        assertEquals("minecraft-weather", skill.name());
        assertEquals("查询和解释 Minecraft 天气", skill.description());
        assertEquals("""
                # Weather

                使用天气工具获取实时天气，再用简洁中文回答。""", skill.instructions());
        assertTrue(skill.triggers().contains("weather"));
        assertTrue(skill.triggers().contains("天气"));
        assertTrue(skill.always());
        assertEquals(Path.of("weather", "SKILL.md"), skill.source());
    }

    @Test
    void rejectsSkillWithoutRequiredMetadata() {
        assertThrows(SkillParseException.class, () -> SkillParser.parse("""
                ---
                name: missing-description
                ---
                instructions
                """, Path.of("SKILL.md")));
    }

    @Test
    void parsesMultilineDescriptionAndQuotedValues() {
        Skill skill = SkillParser.parse("""
                ---
                name: docs
                description: >
                  帮助用户编写和审阅文档。
                triggers:
                  - 文档
                  - docs
                ---
                instructions
                """, Path.of("docs", "SKILL.md"));

        assertEquals("帮助用户编写和审阅文档。", skill.description());
        assertEquals(2, skill.triggers().size());
    }
}
