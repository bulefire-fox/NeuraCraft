package com.bulefire.neuracraft.core.skill;

/**
 * SkillParseException <br>
 * SKILL.md 文档解析失败时抛出的异常.
 * <p>
 * 继承 {@link IllegalArgumentException}, 表示调用方提供了非法内容 (缺失必填的 front matter 字段、
 * 未闭合的 front matter、为空的指令正文等). 消息中统一带上来源路径, 便于在多级目录扫描时定位到具体文件.
 * <p>
 * 该异常属于可恢复错误: {@link SkillManager} 会捕获它并把失败信息收集到
 * {@link SkillManager.ReloadReport#errors()} 中, 单个文件解析失败不会丢弃其他有效技能.
 *
 * @author bulefire_fox
 * @version 1.0
 * @see SkillParser
 * @see SkillManager
 * @see SkillManager.ReloadReport#errors()
 * @since 2.0
 */
public class SkillParseException extends IllegalArgumentException {
    /**
     * 使用错误描述构造异常.
     *
     * @param message 错误描述, 约定格式为 {@code 来源路径: 失败原因}
     */
    public SkillParseException(String message) {
        super(message);
    }

    /**
     * 使用错误描述与原始原因构造异常.
     *
     * @param message 错误描述, 约定格式为 {@code 来源路径: 失败原因}
     * @param cause   触发该异常的底层异常, 例如读取文件时的 {@link java.io.IOException}
     */
    public SkillParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
