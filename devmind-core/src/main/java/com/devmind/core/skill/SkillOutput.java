package com.devmind.core.skill;

import java.util.Map;

public record SkillOutput(
    boolean success,
    String content,
    Map<String, Object> metadata
) {
    public static SkillOutput success(String content) {
        return new SkillOutput(true, content, Map.of());
    }

    public static SkillOutput success(String content, Map<String, Object> metadata) {
        return new SkillOutput(true, content, metadata);
    }

    public static SkillOutput failure(String errorMessage) {
        return new SkillOutput(false, errorMessage, Map.of());
    }
}
