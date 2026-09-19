package com.bulefire.neuracraft.core.skill;

import com.bulefire.neuracraft.compatibility.util.FileUtil;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * SkillManager <br>
 * 技能管理器: 扫描技能目录下的 SKILL.md, 按当前用户消息挑选相关技能, 并拼接为可注入的提示词片段.
 * <p>
 * 职责链路为 "扫描加载 → 热重载检测 → 匹配打分 → 生成上下文":
 * <ul>
 *     <li>扫描: {@link #reload()} 递归遍历技能根目录 (默认 {@code config/neuracraft/skills}), 解析所有 SKILL.md</li>
 *     <li>热重载: {@link #reloadIfChanged()} 通过文件大小与修改时间构成的指纹判断目录是否变化, 变化时才重新扫描</li>
 *     <li>匹配: {@link #select(String)} 依据技能名、触发词与分词重叠度打分, 取分数最高的前若干个</li>
 *     <li>上下文: {@link #buildContext(String)} 把选中的技能拼成 {@code <skills>} XML 片段</li>
 * </ul>
 * <p>
 * 单个文件解析失败只会记录到 {@link #getErrors()}, 不会丢弃其他有效技能.
 * 技能集合、错误列表与指纹均声明为 {@code volatile}, 保证读线程能立即看到重载结果.
 * <p>
 * 通过 {@link #getInstance()} 获取全局单例; 全局实例在 {@code InitCore} 初始化阶段完成首次加载,
 * 由 {@code DeepSeek} 与 {@code GPT} 在构造请求时调用 {@link #buildContext(String)}.
 * 注意: 该类只做匹配与拼接, 不感知具体的模型协议, 由调用方决定注入位置.
 *
 * @author bulefire_fox
 * @version 1.0
 * @see Skill
 * @see SkillParser
 * @see SkillManager.ReloadReport
 * @since 2.0
 */
@Log4j2
public class SkillManager {
    /**
     * 分词正则: 连续的汉字作为一个词, 或由字母/数字/下划线/连字符组成的词.
     */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{IsHan}]+|[\\p{L}\\p{N}_-]+");
    /** 默认单次最多注入的技能数量. */
    private static final int DEFAULT_MAX_SKILLS = 4;
    /** 默认注入上下文的最大字符数, 超出后按顺序截断. */
    private static final int DEFAULT_MAX_CONTEXT_CHARACTERS = 12_000;

    /** 技能根目录, 递归扫描其中的 SKILL.md. */
    private final Path root;
    /** 单次最多注入的技能数量. */
    private final int maxSkills;
    /** 单次注入上下文的最大字符数. */
    private final int maxContextCharacters;
    /** 已加载技能, 键为小写技能名; 整体替换以保证读到的是一致快照. */
    private volatile Map<String, Skill> skills = Map.of();
    /** 最近一次加载过程中的错误信息, 供命令与日志排查. */
    private volatile List<String> errors = List.of();
    /** 技能目录的文件指纹, 用于判断是否需要重载. */
    private volatile String fingerprint = "";

    /**
     * 使用默认技能数量与上下文字数上限构造管理器.
     *
     * @param root 技能根目录
     */
    public SkillManager(@NotNull Path root) {
        this(root, DEFAULT_MAX_SKILLS, DEFAULT_MAX_CONTEXT_CHARACTERS);
    }

    /**
     * 构造管理器.
     *
     * @param root                技能根目录
     * @param maxSkills           单次最多注入的技能数量, 必须大于 0
     * @param maxContextCharacters 单次注入上下文的最大字符数, 必须大于 0
     * @throws IllegalArgumentException 任一上限不大于 0 时抛出
     */
    public SkillManager(@NotNull Path root, int maxSkills, int maxContextCharacters) {
        if (maxSkills <= 0 || maxContextCharacters <= 0) {
            throw new IllegalArgumentException("Skill limits must be greater than zero");
        }
        this.root = root;
        this.maxSkills = maxSkills;
        this.maxContextCharacters = maxContextCharacters;
    }

    /**
     * 获取全局技能管理器实例.
     *
     * @return 全局单例, 技能根目录固定为 {@code config/neuracraft/skills}
     */
    public static @NotNull SkillManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 单例持有者: 借助类初始化机制实现线程安全的惰性单例.
     */
    private static class Holder {
        private static final SkillManager INSTANCE = new SkillManager(FileUtil.base_url.resolve("skills"));
    }

    /**
     * 重新扫描技能目录并重建技能集合, 同时刷新文件指纹.
     * <p>
     * 目录不存在时会自动创建; 扫描过程本身失败 (如 IO 异常) 时保留上一次成功加载的技能集合,
     * 只更新错误列表, 避免因为一次瞬时故障把已有技能全部清空.
     *
     * @return 加载报告, 包含本次成功加载的技能数量与全部错误信息
     */
    public synchronized @NotNull ReloadReport reload() {
        Map<String, Skill> loaded = new LinkedHashMap<>();
        List<String> loadErrors = new ArrayList<>();
        boolean scanSucceeded = true;
        try {
            Files.createDirectories(root);
            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> "SKILL.md".equalsIgnoreCase(path.getFileName().toString()))
                        .sorted(Comparator.comparing(Path::toString))
                        .forEach(path -> loadSkill(path, loaded, loadErrors));
            }
        } catch (IOException e) {
            scanSucceeded = false;
            loadErrors.add(root + ": " + e.getMessage());
        }

        if (scanSucceeded) {
            skills = Map.copyOf(loaded);
            fingerprint = calculateFingerprint();
        }
        errors = List.copyOf(loadErrors);
        if (!loadErrors.isEmpty()) {
            log.warn("Loaded {} skills with {} errors from {}", loaded.size(), loadErrors.size(), root);
        } else {
            log.info("Loaded {} skills from {}", loaded.size(), root);
        }
        return new ReloadReport(loaded.size(), loadErrors);
    }

    /**
     * 检测技能目录变化并在需要时重新加载.
     * <p>
     * 先做一次无锁指纹比对, 指纹变化后再进入同步块二次确认, 避免并发调用时重复扫描.
     * 该方法由 {@link #select(String)} 自动调用, 因此新增或修改 SKILL.md 后无需重启即可生效.
     */
    public void reloadIfChanged() {
        String currentFingerprint = calculateFingerprint();
        if (!currentFingerprint.equals(fingerprint)) {
            synchronized (this) {
                if (!currentFingerprint.equals(fingerprint)) {
                    reload();
                }
            }
        }
    }

    /**
     * 获取已加载的全部技能.
     *
     * @return 按技能名排序的技能列表; 未加载任何技能时返回空列表
     */
    public @NotNull List<Skill> getSkills() {
        return skills.values().stream()
                .sorted(Comparator.comparing(Skill::name))
                .toList();
    }

    /**
     * 获取最近一次加载过程中收集到的错误信息.
     *
     * @return 错误信息列表, 每项形如 {@code 来源路径: 失败原因}; 无错误时返回空列表
     */
    public @NotNull List<String> getErrors() {
        return errors;
    }

    /**
     * 根据用户消息挑选相关技能.
     * <p>
     * 先触发一次热重载检测, 再对每个技能打分: 技能名整体命中权重最高, 触发词命中次之,
     * 最后按分词重叠度补充; 标记为 {@link Skill#always()} 的技能无条件入选.
     * 结果按得分降序 (同分按技能名升序) 排序后截取前 {@code maxSkills} 个.
     *
     * @param query 当前用户消息原文
     * @return 选中的技能列表, 按相关度降序; 无匹配时返回空列表
     */
    public @NotNull List<Skill> select(@NotNull String query) {
        reloadIfChanged();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        Set<String> queryTokens = tokens(query);
        List<ScoredSkill> candidates = new ArrayList<>();
        for (Skill skill : skills.values()) {
            int score = score(skill, normalizedQuery, queryTokens);
            if (skill.always() || score > 0) {
                candidates.add(new ScoredSkill(skill, score));
            }
        }
        return candidates.stream()
                .sorted(Comparator.comparingInt(ScoredSkill::score).reversed()
                        .thenComparing(item -> item.skill().name()))
                .limit(maxSkills)
                .map(ScoredSkill::skill)
                .toList();
    }

    /**
     * 构建注入到模型提示词中的技能上下文片段.
     * <p>
     * 输出为 {@code <skills>} 包裹的 XML 片段, 每个技能为一个 {@code <skill>} 元素,
     * 属性值经过转义以避免破坏结构. 超过 {@code maxContextCharacters} 时会在末尾截断,
     * 仅当剩余空间大于 32 字符时才写入部分内容, 避免留下无意义的残片.
     *
     * @param query 当前用户消息原文, 用于挑选相关技能
     * @return 技能上下文片段; 没有选中任何技能时返回空字符串
     */
    public @NotNull String buildContext(@NotNull String query) {
        List<Skill> selected = select(query);
        if (selected.isEmpty()) {
            return "";
        }
        StringBuilder context = new StringBuilder("""
                <skills>
                The following skill instructions are relevant to the current request.
                Follow them when applicable, while treating the user's request as the task.
                
                """);
        for (Skill skill : selected) {
            String entry = "<skill name=\"" + escape(skill.name()) + "\" description=\""
                    + escape(skill.description()) + "\">\n"
                    + skill.instructions() + "\n</skill>\n\n";
            if (context.length() + entry.length() + 9 > maxContextCharacters) {
                int remaining = maxContextCharacters - context.length() - 9;
                if (remaining > 32) {
                    context.append(entry, 0, Math.min(entry.length(), remaining));
                    context.append('\n');
                }
                break;
            }
            context.append(entry);
        }
        context.append("</skills>");
        return context.toString();
    }

    /**
     * 解析并登记单个技能文件.
     * <p>
     * 技能名重复时只记录错误并保留先加载的那一个 (路径按字典序排序, 结果稳定);
     * 解析失败同样只记录错误, 不影响其他技能加载.
     *
     * @param path        SKILL.md 路径
     * @param loaded      已加载技能的收集容器, 键为小写技能名
     * @param loadErrors  错误信息收集容器
     */
    private static void loadSkill(Path path, Map<String, Skill> loaded, List<String> loadErrors) {
        try {
            Skill skill = SkillParser.parse(path);
            String key = skill.name().toLowerCase(Locale.ROOT);
            if (loaded.containsKey(key)) {
                loadErrors.add(path + ": duplicate skill name " + skill.name());
                return;
            }
            loaded.put(key, skill);
        } catch (IOException | SkillParseException e) {
            loadErrors.add(path + ": " + e.getMessage());
        }
    }

    /**
     * 计算单个技能与用户消息的相关度得分.
     * <p>
     * 打分梯度: 技能名整体出现在消息中 {@code +10000}; 触发词整体出现 {@code +1000};
     * 触发词作为独立词元命中 {@code +100}; 技能名词元重叠每个 {@code +40}; 描述词元重叠每个 {@code +10}.
     *
     * @param skill           待打分的技能
     * @param normalizedQuery 已转为小写的用户消息
     * @param queryTokens     用户消息的词元集合
     * @return 相关度得分, 0 表示不相关
     */
    private static int score(Skill skill, String normalizedQuery, Set<String> queryTokens) {
        int score = 0;
        String normalizedName = skill.name().toLowerCase(Locale.ROOT);
        if (normalizedQuery.contains(normalizedName)) {
            score += 10_000;
        }
        for (String trigger : skill.triggers()) {
            String normalizedTrigger = trigger.toLowerCase(Locale.ROOT);
            if (normalizedQuery.contains(normalizedTrigger)) {
                score += 1_000;
            }
            if (queryTokens.contains(normalizedTrigger)) {
                score += 100;
            }
        }
        score += overlap(queryTokens, tokens(skill.name())) * 40;
        score += overlap(queryTokens, tokens(skill.description())) * 10;
        return score;
    }

    /**
     * 统计两个词元集合的交集大小.
     *
     * @param left  左侧词元集合
     * @param right 右侧词元集合
     * @return 同时出现在两个集合中的词元数量
     */
    private static int overlap(Set<String> left, Set<String> right) {
        int count = 0;
        for (String token : right) {
            if (left.contains(token)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 将文本切分为词元集合.
     * <p>
     * 中文没有空格分隔, 因此在提取出连续汉字串之后, 还会补充长度 2 到 4 的所有子串,
     * 使 "现在天气怎么样" 这类消息能够命中 "天气" 这样的短触发词.
     *
     * @param text 待切分的文本
     * @return 小写词元集合, 保持插入顺序
     */
    private static Set<String> tokens(String text) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String token = matcher.group();
            result.add(token);
            if (token.codePoints().allMatch(codePoint ->
                    Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN)) {
                int[] codePoints = token.codePoints().toArray();
                for (int size = 2; size <= Math.min(4, codePoints.length); size++) {
                    for (int start = 0; start + size <= codePoints.length; start++) {
                        result.add(new String(codePoints, start, size));
                    }
                }
            }
        }
        return result;
    }

    /**
     * 转义 XML 属性值中的特殊字符, 防止技能内容破坏 {@code <skill>} 标签结构.
     *
     * @param value 原始文本
     * @return 转义后的文本
     */
    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /**
     * 计算技能目录的文件指纹.
     * <p>
     * 指纹由所有 SKILL.md 的路径、文件大小与最后修改时间拼接而成, 用于低成本判断目录是否变化.
     * 单个文件读取失败时以 {@code unreadable} 占位, 整体失败时返回错误类型名, 均不会抛出异常.
     *
     * @return 目录指纹; 目录不存在时为 {@code missing}, 无 SKILL.md 时为 {@code empty}
     */
    private String calculateFingerprint() {
        try {
            if (!Files.exists(root)) {
                return "missing";
            }
            try (Stream<Path> paths = Files.walk(root)) {
                return paths.filter(Files::isRegularFile)
                        .filter(path -> "SKILL.md".equalsIgnoreCase(path.getFileName().toString()))
                        .sorted(Comparator.comparing(Path::toString))
                        .map(path -> {
                            try {
                                return path + ":" + Files.size(path) + ":"
                                        + Files.getLastModifiedTime(path).toMillis();
                            } catch (IOException e) {
                                return path + ":unreadable";
                            }
                        })
                        .reduce((left, right) -> left + "|" + right)
                        .orElse("empty");
            }
        } catch (IOException e) {
            return "unreadable:" + e.getClass().getName();
        }
    }

    /**
     * 一次重载的结果报告.
     *
     * @param loaded 成功加载的技能数量
     * @param errors 加载过程中收集到的错误信息, 允许为 {@code null} (会转为空列表)
     */
    public record ReloadReport(int loaded, List<String> errors) {
        /**
         * 紧凑构造函数: 保证 {@code errors} 非空且不可变.
         */
        public ReloadReport {
            errors = errors == null ? List.of() : List.copyOf(errors);
        }
    }

    /**
     * 匹配阶段的内部中间结果: 技能与其相关度得分.
     *
     * @param skill 候选技能
     * @param score 该技能的相关度得分
     */
    private record ScoredSkill(Skill skill, int score) {
    }
}
