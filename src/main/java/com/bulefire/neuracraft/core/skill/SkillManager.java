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

@Log4j2
public class SkillManager {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{IsHan}]+|[\\p{L}\\p{N}_-]+");
    private static final int DEFAULT_MAX_SKILLS = 4;
    private static final int DEFAULT_MAX_CONTEXT_CHARACTERS = 12_000;

    private final Path root;
    private final int maxSkills;
    private final int maxContextCharacters;
    private volatile Map<String, Skill> skills = Map.of();
    private volatile List<String> errors = List.of();
    private volatile String fingerprint = "";

    public SkillManager(@NotNull Path root) {
        this(root, DEFAULT_MAX_SKILLS, DEFAULT_MAX_CONTEXT_CHARACTERS);
    }

    public SkillManager(@NotNull Path root, int maxSkills, int maxContextCharacters) {
        if (maxSkills <= 0 || maxContextCharacters <= 0) {
            throw new IllegalArgumentException("Skill limits must be greater than zero");
        }
        this.root = root;
        this.maxSkills = maxSkills;
        this.maxContextCharacters = maxContextCharacters;
    }

    public static @NotNull SkillManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final SkillManager INSTANCE = new SkillManager(FileUtil.base_url.resolve("skills"));
    }

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

    public @NotNull List<Skill> getSkills() {
        return skills.values().stream()
                .sorted(Comparator.comparing(Skill::name))
                .toList();
    }

    public @NotNull List<String> getErrors() {
        return errors;
    }

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

    private static int overlap(Set<String> left, Set<String> right) {
        int count = 0;
        for (String token : right) {
            if (left.contains(token)) {
                count++;
            }
        }
        return count;
    }

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

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

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

    public record ReloadReport(int loaded, List<String> errors) {
        public ReloadReport {
            errors = errors == null ? List.of() : List.copyOf(errors);
        }
    }

    private record ScoredSkill(Skill skill, int score) {
    }
}
