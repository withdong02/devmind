package com.devmind.core.skill;

import java.util.Map;

public record SkillInput(
    Map<String, Object> parameters
) {
    public static SkillInput of(Map<String, Object> parameters) {
        return new SkillInput(parameters);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value == null) return null;
        return (T) value;
    }

    public String getString(String key) {
        return get(key, String.class);
    }
}
