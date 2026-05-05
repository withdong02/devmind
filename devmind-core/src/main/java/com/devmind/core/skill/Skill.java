package com.devmind.core.skill;

/**
 * Core interface for all DevMind skills.
 * Each skill is a self-contained capability that can be invoked by agents.
 */
public interface Skill {

    /**
     * Returns the skill's metadata (name, description, input schema).
     */
    SkillDefinition definition();

    /**
     * Executes the skill with the given input and context.
     */
    SkillOutput execute(SkillInput input, SkillContext context);

    /**
     * Determines if this skill can handle the given user intent.
     * Used for automatic routing from natural language to skill.
     */
    boolean canHandle(String userIntent);
}
