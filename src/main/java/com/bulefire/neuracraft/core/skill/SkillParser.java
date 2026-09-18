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
 * Parses the small YAML front matter used by SKILL.md files without requiring
 * a YAML runtime dependency.
 */
public final class SkillParser {
    private SkillParser() {
    }

    public static @NotNull Skill parse(@NotNull Path source) throws IOException {
        return parse(Files.readString(source, StandardCharsets.UTF_8), source);
    }

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
        Set<String> triggers = new LinkedHashSet<>(frontMatter.lists().getOrDefault("triggers", List.of()));
        triggers.addAll(frontMatter.lists().getOrDefault("keywords", List.of()));
        boolean always = parseBoolean(frontMatter.values().get("always"), false, source);
        return new Skill(name, description, instructions, triggers, always, source);
    }

    private static int findClosingFrontMatter(String[] lines, Path source) {
        for (int i = 1; i < lines.length; i++) {
            if ("---".equals(lines[i].trim()) || "...".equals(lines[i].trim())) {
                return i;
            }
        }
        throw error(source, "skill front matter is not closed");
    }

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

    private static String required(Map<String, String> values, String key, Path source) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw error(source, "required front matter field is missing: " + key);
        }
        return value;
    }

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

    private static SkillParseException error(Path source, String message) {
        return new SkillParseException(source + ": " + message);
    }

    private record FrontMatter(Map<String, String> values, Map<String, List<String>> lists) {
    }
}
