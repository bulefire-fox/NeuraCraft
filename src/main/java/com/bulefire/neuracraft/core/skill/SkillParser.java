package com.bulefire.neuracraft.core.skill;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;

/**
 * SkillParser <br>
 * SKILL.md 文档解析器: 把 Markdown 文本解析为 {@link Skill} 实例.
 * <p>
 * 文件结构由 {@code ---} 包裹的 YAML 风格 front matter 加上其后的 Markdown 正文组成:
 * <pre>
 *     {@code
 *     ---
 *     name: weather
 *     description: 查询 Minecraft 天气
 *     triggers: [天气, weather]
 *     always: false
 *     ---
 *     正文指令...
 *     }
 * </pre>
 * 为保证轻量, 这里只实现了 SKILL.md 所需的 YAML 子集: 标量键值对、单行列表
 * {@code [a, b]}、块列表 {@code - a}、以及块标量 {@code |} (保留换行) 与 {@code >} (折叠为单行),
 * 不引入任何 YAML 运行时依赖. 键名统一转小写, 因此大小写不敏感.
 * <p>
 * 该类为无状态工具类, 不可实例化, 所有方法均为静态且线程安全.
 *
 * @author bulefire_fox
 * @version 1.0
 * @see Skill
 * @see SkillParseException
 * @see SkillManager#reload()
 * @since 2.0
 */
public final class SkillParser {
    /**
     * 私有构造函数: 工具类不允许实例化.
     */
    private SkillParser() {
    }

    /**
     * 从文件读取 SKILL.md 并解析.
     * <p>
     * 文件按 UTF-8 解码; 若文件带 BOM, 会在 {@link #parse(String, Path)} 中自动剥离.
     *
     * @param source SKILL.md 文件路径
     * @return 解析得到的 {@link Skill} 实例
     * @throws IOException          读取文件失败时抛出
     * @throws SkillParseException  文档内容不符合 SKILL.md 格式时抛出
     */
    public static @NotNull Skill parse(@NotNull Path source) throws IOException {
        return parse(Files.readString(source, StandardCharsets.UTF_8), source);
    }

    /**
     * 解析 SKILL.md 文本内容.
     * <p>
     * 解析前会统一换行符 (CRLF / CR 归一为 LF) 并剥离 BOM. 正文之外的空白会被去除,
     * 正文为空视为非法文档并抛出异常.
     *
     * @param markdown SKILL.md 的完整文本
     * @param source   来源路径, 仅用于拼接错误信息与写入 {@link Skill#source()}
     * @return 解析得到的 {@link Skill} 实例
     * @throws SkillParseException 未以 front matter 开头、front matter 未闭合、
     *                             缺少必填字段 {@code name} / {@code description},
     *                             正文为空, 或 {@code always} 取值非布尔时抛出
     */
    public static @NotNull Skill parse(@NotNull String markdown, @NotNull Path source) {
        String normalized = markdown.replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.startsWith("\uFEFF")) {
            normalized = normalized.substring(1);
        }
        String[] lines = normalized.split("\n", -1);
        if (lines.length == 0 || !"---".equals(lines[0].trim())) {
            throw error(source, "SKILL.md must start with YAML front matter");
        }

        int closingLine = findClosingFrontMatter(lines, source);
        FrontMatter frontMatter = parseFrontMatter(lines, closingLine, source);
        String instructions = String.join(
                "\n",
                Arrays.copyOfRange(lines, closingLine + 1, lines.length)
        ).trim();
        if (instructions.isBlank()) {
            throw error(source, "skill instructions must not be blank");
        }

        String name = required(frontMatter.values(), "name", source);
        String description = required(frontMatter.values(), "description", source);
        // triggers 与 keywords 是等价写法, 合并后交给 Skill 去重
        Set<String> triggers = new LinkedHashSet<>(frontMatter.lists().getOrDefault("triggers", List.of()));
        triggers.addAll(frontMatter.lists().getOrDefault("keywords", List.of()));
        boolean always = parseBoolean(frontMatter.values().get("always"), false, source);
        return new Skill(name, description, instructions, triggers, always, source);
    }

    /**
     * 查找 front matter 的结束行下标.
     *
     * @param lines  已按行切分的文档内容
     * @param source 来源路径, 仅用于拼接错误信息
     * @return 结束行下标, 即 {@code ---} 或 {@code ...} 所在行 (从下标 1 开始查找)
     * @throws SkillParseException 遍历到文件末尾仍未找到结束行时抛出
     */
    private static int findClosingFrontMatter(String[] lines, Path source) {
        for (int i = 1; i < lines.length; i++) {
            if ("---".equals(lines[i].trim()) || "...".equals(lines[i].trim())) {
                return i;
            }
        }
        throw error(source, "skill front matter is not closed");
    }

    /**
     * 解析 front matter 区间内的键值对与列表.
     * <p>
     * 结果按标量值 ( {@code values} ) 与列表值 ( {@code lists} ) 分开存放. 空行与以 {@code #}
     * 开头的注释行被忽略; {@code - } 开头的行归属到最近一次出现的键, 因此块列表必须写在对应键之下.
     *
     * @param lines       已按行切分的文档内容
     * @param closingLine front matter 结束行下标 (不包含该行)
     * @param source      来源路径, 仅用于拼接错误信息
     * @return 解析结果 {@link FrontMatter}
     * @throws SkillParseException 某行不含冒号而无法识别为键值对时抛出
     */
    private static FrontMatter parseFrontMatter(String[] lines, int closingLine, Path source) {
        Map<String, String> values = new LinkedHashMap<>();
        Map<String, List<String>> lists = new LinkedHashMap<>();
        String activeKey = null;
        for (int i = 1; i < closingLine; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            if (trimmed.isBlank() || trimmed.startsWith("#")) {
                continue;
            }
            if (trimmed.startsWith("- ") && activeKey != null) {
                lists.computeIfAbsent(activeKey, ignored -> new ArrayList<>())
                        .add(unquote(trimmed.substring(2).trim()));
                continue;
            }

            int colon = line.indexOf(':');
            if (colon <= 0) {
                throw error(source, "invalid front matter line " + (i + 1) + ": " + line);
            }
            String key = line.substring(0, colon).trim().toLowerCase(Locale.ROOT);
            String value = line.substring(colon + 1).trim();
            activeKey = key;

            if ("|".equals(value) || ">".equals(value)) {
                // 块标量: 缩进行属于该键; 遇到顶格的下一行时回退一步交还给外层循环
                StringBuilder block = new StringBuilder();
                boolean folded = ">".equals(value);
                i++;
                while (i < closingLine) {
                    String blockLine = lines[i];
                    if (!blockLine.isBlank() && !Character.isWhitespace(blockLine.charAt(0))) {
                        i--;
                        break;
                    }
                    String content = blockLine.stripLeading();
                    if (folded && !content.isBlank()) {
                        if (block.length() > 0) {
                            block.append(' ');
                        }
                        block.append(content);
                    } else {
                        if (block.length() > 0) {
                            block.append('\n');
                        }
                        block.append(content);
                    }
                    i++;
                }
                values.put(key, unquote(block.toString().trim()));
                continue;
            }

            if (value.startsWith("[") && value.endsWith("]")) {
                lists.put(key, parseInlineList(value.substring(1, value.length() - 1)));
            } else {
                values.put(key, unquote(value));
            }
        }
        return new FrontMatter(values, lists);
    }

    /**
     * 解析单行列表 {@code [a, b, c]} 的内容部分.
     *
     * @param value 去掉方括号后的列表文本
     * @return 解析出的列表项, 已去除引号与空白并过滤空项
     */
    private static List<String> parseInlineList(String value) {
        List<String> result = new ArrayList<>();
        for (String item : value.split(",")) {
            String normalized = unquote(item.trim());
            if (!normalized.isBlank()) {
                result.add(normalized);
            }
        }
        return result;
    }

    /**
     * 读取必填的标量字段.
     *
     * @param values front matter 中的标量键值对
     * @param key    字段名 (小写)
     * @param source 来源路径, 仅用于拼接错误信息
     * @return 字段值
     * @throws SkillParseException 字段缺失或为空白时抛出
     */
    private static String required(Map<String, String> values, String key, Path source) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw error(source, "required front matter field is missing: " + key);
        }
        return value;
    }

    /**
     * 解析布尔字段, 只接受 {@code true} 与 {@code false} (大小写不敏感).
     *
     * @param value    字段原始值, {@code null} 或空白时直接返回兜底值
     * @param fallback 字段未提供时使用的默认值
     * @param source   来源路径, 仅用于拼接错误信息
     * @return 解析出的布尔值
     * @throws SkillParseException 字段值既不是 {@code true} 也不是 {@code false} 时抛出
     */
    private static boolean parseBoolean(String value, boolean fallback, Path source) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw error(source, "front matter field always must be true or false");
    }

    /**
     * 去掉成对的单引号或双引号, 并把双引号内的 {@code \n} 转义还原为换行.
     *
     * @param value 原始文本 (通常已 trim)
     * @return 去引号后的文本; 不是成对引号包裹时原样返回
     */
    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1).replace("\\n", "\n");
            }
        }
        return value;
    }

    /**
     * 构造带来源路径前缀的解析异常.
     *
     * @param source  来源路径
     * @param message 失败原因
     * @return 统一格式为 {@code 来源路径: 失败原因} 的异常实例
     */
    private static SkillParseException error(Path source, String message) {
        return new SkillParseException(source + ": " + message);
    }

    /**
     * front matter 的解析中间结果.
     *
     * @param values 标量键值对, 键为小写字段名
     * @param lists  列表键值对, 键为小写字段名
     */
    private record FrontMatter(Map<String, String> values, Map<String, List<String>> lists) {
    }
}
