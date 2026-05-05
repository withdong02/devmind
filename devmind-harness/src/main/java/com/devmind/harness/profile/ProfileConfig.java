package com.devmind.harness.profile;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

/**
 * YAML-based profile configuration engine.
 * Profiles define hook chains, model settings, and guardrails.
 */
@Component
public class ProfileConfig {

    private Map<String, Object> profiles = new LinkedHashMap<>();
    private String activeProfile = "default";

    public ProfileConfig() {
        loadDefaults();
    }

    @SuppressWarnings("unchecked")
    private void loadDefaults() {
        Yaml yaml = new Yaml();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("harness-profiles.yml")) {
            if (is != null) {
                Map<String, Object> root = yaml.load(is);
                if (root != null && root.containsKey("agent-profiles")) {
                    profiles = (Map<String, Object>) root.get("agent-profiles");
                }
            }
        } catch (Exception e) {
            // Use programmatic defaults
        }

        if (profiles.isEmpty()) {
            profiles.put("default", createDefaultProfile());
            profiles.put("safe-mode", createSafeModeProfile());
        }
    }

    private Map<String, Object> createDefaultProfile() {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("description", "Default profile with standard guardrails");
        profile.put("model", "mimo-v2.5-pro");
        profile.put("temperature", 0.7);

        Map<String, Object> hooks = new LinkedHashMap<>();
        hooks.put("pre-tool-call", List.of(
                Map.of("type", "guardrail"),
                Map.of("type", "logging")
        ));
        hooks.put("post-tool-call", List.of(
                Map.of("type", "logging")
        ));
        profile.put("hooks", hooks);

        Map<String, Object> guardrails = new LinkedHashMap<>();
        guardrails.put("blocked-commands", List.of("rm -rf /", "DROP TABLE", "git push --force"));
        profile.put("guardrails", guardrails);

        return profile;
    }

    private Map<String, Object> createSafeModeProfile() {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("description", "Safe mode with strict guardrails and rate limiting");
        profile.put("model", "mimo-v2.5-pro");
        profile.put("temperature", 0.3);

        Map<String, Object> hooks = new LinkedHashMap<>();
        hooks.put("pre-tool-call", List.of(
                Map.of("type", "rate-limiting", "config", Map.of("max-calls-per-minute", 10)),
                Map.of("type", "guardrail", "config", Map.of("blocked-commands", List.of("rm -rf", "git push --force", "DROP TABLE")))
        ));
        hooks.put("post-tool-call", List.of(
                Map.of("type", "logging")
        ));
        profile.put("hooks", hooks);

        return profile;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getActiveProfile() {
        return (Map<String, Object>) profiles.getOrDefault(activeProfile, profiles.get("default"));
    }

    public void setActiveProfile(String name) {
        if (profiles.containsKey(name)) {
            this.activeProfile = name;
        }
    }

    public String getActiveProfileName() {
        return activeProfile;
    }

    public Map<String, Object> getAllProfiles() {
        return Map.copyOf(profiles);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getGuardrails() {
        Map<String, Object> profile = getActiveProfile();
        Object guardrails = profile.get("guardrails");
        if (guardrails instanceof Map) {
            return List.of((Map<String, Object>) guardrails);
        }
        return List.of();
    }
}
