package com.devmind.api.controller;

import com.devmind.core.skill.*;
import com.devmind.skills.engine.SkillRouter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Skills", description = "Skill registry, routing, and execution")
@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final SkillRegistry skillRegistry;
    private final SkillRouter skillRouter;

    public SkillController(SkillRegistry skillRegistry, SkillRouter skillRouter) {
        this.skillRegistry = skillRegistry;
        this.skillRouter = skillRouter;
    }

    @Operation(summary = "List all registered skills")
    @GetMapping
    public ResponseEntity<List<SkillDefinition>> listSkills() {
        List<SkillDefinition> skills = skillRegistry.getAllSkills().stream()
                .map(Skill::definition)
                .toList();
        return ResponseEntity.ok(skills);
    }

    @Operation(summary = "Get skill definition by ID")
    @GetMapping("/{id}")
    public ResponseEntity<SkillDefinition> getSkill(@PathVariable String id) {
        return skillRegistry.getSkillById(id)
                .map(s -> ResponseEntity.ok(s.definition()))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Execute a skill directly")
    @PostMapping("/{id}/execute")
    public ResponseEntity<Map<String, Object>> executeSkill(
            @PathVariable String id,
            @RequestBody Map<String, Object> input) {

        return skillRegistry.getSkillById(id)
                .map(skill -> {
                    SkillInput skillInput = SkillInput.of(input);
                    SkillContext context = SkillContext.of("api-direct", "system");
                    SkillOutput output = skill.execute(skillInput, context);
                    return ResponseEntity.ok(Map.of(
                            "success", output.success(),
                            "content", output.content(),
                            "metadata", output.metadata()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Route a user message to the best matching skill")
    @PostMapping("/route")
    public ResponseEntity<Map<String, Object>> routeIntent(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return skillRouter.route(message)
                .<ResponseEntity<Map<String, Object>>>map(skill -> ResponseEntity.ok(Map.of(
                        "matched", (Object) true,
                        "skillId", skill.definition().id(),
                        "skillName", skill.definition().name(),
                        "description", skill.definition().description()
                )))
                .orElse(ResponseEntity.ok(Map.of("matched", (Object) false)));
    }
}
