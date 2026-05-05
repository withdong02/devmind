package com.devmind.api.controller;

import com.devmind.core.context.ContextComponent;
import com.devmind.core.context.ContextPlan;
import com.devmind.context.service.ContextService;
import com.devmind.context.template.PromptTemplateEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Context", description = "Context engineering: token budget, assembly, templates")
@RestController
@RequestMapping("/api/v1/context")
public class ContextController {

    private final ContextService contextService;
    private final PromptTemplateEngine templateEngine;

    public ContextController(ContextService contextService, PromptTemplateEngine templateEngine) {
        this.contextService = contextService;
        this.templateEngine = templateEngine;
    }

    @Operation(summary = "Build and render optimized context for a query")
    @PostMapping("/build")
    public ResponseEntity<Map<String, Object>> buildContext(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "user");
        String query = (String) body.getOrDefault("query", "");
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) body.get("history");

        ContextPlan plan = contextService.buildContext(userId, query, history != null ? history : List.of());
        String rendered = contextService.renderPrompt(plan);

        List<Map<String, Object>> components = new ArrayList<>();
        for (ContextComponent comp : plan.components()) {
            components.add(Map.of(
                    "id", comp.id(),
                    "type", comp.type().toString(),
                    "tokenCount", comp.tokenCount(),
                    "priorityScore", comp.priorityScore(),
                    "preview", comp.content().length() > 200 ? comp.content().substring(0, 200) + "..." : comp.content()
            ));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTokens", plan.totalTokens());
        result.put("tokenBudget", plan.tokenBudget());
        result.put("withinBudget", plan.isWithinBudget());
        result.put("componentCount", plan.components().size());
        result.put("components", components);
        result.put("renderedPreview", rendered.length() > 2000 ? rendered.substring(0, 2000) + "..." : rendered);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get current token budget allocation")
    @GetMapping("/budget")
    public ResponseEntity<Map<String, Object>> getBudget() {
        var allocations = contextService.getBudgetManager().getAllocations();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBudget", contextService.getBudgetManager().getTotalBudget());
        Map<String, Integer> allocMap = new LinkedHashMap<>();
        allocations.forEach((k, v) -> allocMap.put(k.toString(), v));
        result.put("allocations", allocMap);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Update token budget")
    @PutMapping("/budget")
    public ResponseEntity<Map<String, Object>> updateBudget(@RequestBody Map<String, Object> body) {
        int total = ((Number) body.getOrDefault("totalBudget", 4096)).intValue();
        contextService.getBudgetManager().setTotalBudget(total);
        return ResponseEntity.ok(Map.of("totalBudget", total, "status", "updated"));
    }

    @Operation(summary = "List all prompt templates")
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTemplates() {
        Map<String, Object> result = new LinkedHashMap<>();
        templateEngine.getAllTemplates().forEach((key, template) -> {
            result.put(key, Map.of("version", template.version(), "preview",
                    template.content().length() > 300 ? template.content().substring(0, 300) + "..." : template.content()));
        });
        return ResponseEntity.ok(result);
    }
}
