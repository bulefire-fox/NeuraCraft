package com.bulefire.neuracraft.core.skill;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Skill <br>
 * 一份已解析的 SKILL.md 技能文档, 是不可变的值对象.
 * <p>
 * 一个技能由三部分组成: 用于匹配的元数据 ( {@link #name()} 与 {@link #triggers()} ),
 * 供模型判断是否适用的 {@link #description()}, 以及真正注入提示词的
 * {@link #instructions()} 正文.
 * <p>
 * 本类型只负责携带数据, 目录扫描与文件读取由 {@link SkillParser} 完成,
 * 加载与匹配由 {@link SkillManager} 完成. 紧凑构造函数会对字段做规范化,
 * 因此任意一个实例都是自洽的: 文本字段非空白, 触发词去空去重, 来源路径非空.
 *
 * @param name         技能名称, 全局唯一标识; 在 {@link SkillManager} 中按小写去重后作为键
 * @param description  技能用途的单行描述, 用于参与匹配打分, 不应为空
 * @param instructions Markdown 正文, 即注入到模型提示词中的技能指令
 * @param triggers     触发词集合, 命中用户消息时提升该技能的匹配得分
 * @param always       为 {@code true} 时不参与打分, 任何查询都会注入该技能 (如安全约束)
 * @param source       该技能的 SKILL.md 来源路径, 仅用于报错与调试定位
 * @author bulefire_fox
 * @version 1.0
 * @see SkillParser
 * @see SkillManager
 * @see SkillParseException
 * @since 2.0
 */
public record Skill(
        String name,
        String description,
        String instructions,
        Set<String> triggers,
        boolean always,
        Path source
) {
    /**
     * 紧凑构造函数: 校验并规范化各个字段.
     *
     * @throws SkillParseException 当 {@code name} / {@code description} / {@code instructions} 为空或全为空白时
     */
    public Skill {
        name = requireText(name, "name");
        description = requireText(description, "description");
        instructions = requireText(instructions, "instructions");
        triggers = normalizeTriggers(triggers);
        source = source == null ? Path.of("SKILL.md") : source;
    }

    /**
     * 校验文本字段非空白, 并去除首尾空白.
     *
     * @param value 待校验的文本
     * @param field 字段名, 仅用于拼接错误信息
     * @return 去除首尾空白后的文本
     * @throws SkillParseException 当文本为 {@code null} 或全为空白时
     */
    private static @NotNull String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new SkillParseException("Skill " + field + " must not be blank");
        }
        return value.trim();
    }

    /**
     * 规范化触发词集合: 过滤 {@code null} 与空白项, 去除首尾空白并去重.
     *
     * @param values 原始触发词集合, 允许为 {@code null}
     * @return 不可变的触发词集合; {@code values} 为 {@code null} 时返回空集合
     */
    private static @NotNull Set<String> normalizeTriggers(Set<String> values) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (values != null) {
            values.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(String::trim)
                    .forEach(normalized::add);
        }
        return Set.copyOf(normalized);
    }

    /**
     * 获取按字典序排序后的触发词列表, 便于输出稳定的提示词与日志内容.
     *
     * @return 排序后的触发词列表; 无触发词时返回空列表
     */
    public @NotNull List<String> triggerList() {
        return triggers.stream().sorted().toList();
    }
}
