package com.devmind.core.skill;

import java.util.List;
import java.util.Optional;

/**
 * Registry for discovering and managing available skills.
 */
public interface SkillRegistry {

    /**
     * Returns all registered skills.
     */
    List<Skill> getAllSkills();

    /**
     * Finds a skill by its ID.
     */
    Optional<Skill> getSkillById(String skillId);

    /**
     * Finds the best skill to handle the given user intent.
     */
    Optional<Skill> findSkillForIntent(String userIntent);
}
