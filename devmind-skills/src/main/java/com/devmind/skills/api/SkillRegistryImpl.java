package com.devmind.skills.api;

import com.devmind.core.skill.Skill;
import com.devmind.core.skill.SkillRegistry;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class SkillRegistryImpl implements SkillRegistry {

    private final List<Skill> skills;

    public SkillRegistryImpl(List<Skill> skills) {
        this.skills = skills;
    }

    @Override
    public List<Skill> getAllSkills() {
        return List.copyOf(skills);
    }

    @Override
    public Optional<Skill> getSkillById(String skillId) {
        return skills.stream()
                .filter(s -> s.definition().id().equals(skillId))
                .findFirst();
    }

    @Override
    public Optional<Skill> findSkillForIntent(String userIntent) {
        return skills.stream()
                .filter(s -> s.canHandle(userIntent))
                .max(Comparator.comparingInt(s -> s.definition().tags().size()));
    }
}
