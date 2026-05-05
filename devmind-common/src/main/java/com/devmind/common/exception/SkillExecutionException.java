package com.devmind.common.exception;

public class SkillExecutionException extends DevMindException {

    private final String skillId;

    public SkillExecutionException(String skillId, String message) {
        super(message, "SKILL_EXECUTION_ERROR");
        this.skillId = skillId;
    }

    public SkillExecutionException(String skillId, String message, Throwable cause) {
        super(message, cause);
        this.skillId = skillId;
    }

    public String getSkillId() { return skillId; }
}
