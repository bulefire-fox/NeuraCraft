package com.bulefire.neuracraft.core.skill;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A parsed SKILL.md document.
 */
public record Skill(
        String name,
        String description,
        String instructions,
        Set<String> triggers,
        boolean always,
        Path source
) {
    public Skill {
        name = requireText(name, "name");
        description = requireText(description, "description");
        instructions = requireText(instructions, "instructions");
        triggers = normalizeTriggers(triggers);
        source = source == null ? Path.of("SKILL.md") : source;
    }

    private static @NotNull String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new SkillParseException("Skill " + field + " must not be blank");
        }
        return value.trim();
    }

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

    public @NotNull List<String> triggerList() {
        return triggers.stream().sorted().toList();
    }
}
