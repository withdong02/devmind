package com.devmind.core.skill;

import java.util.List;
import java.util.Map;

public record SkillDefinition(
    String id,
    String name,
    String description,
    Map<String, String> inputSchema,
    List<String> tags
) {}
