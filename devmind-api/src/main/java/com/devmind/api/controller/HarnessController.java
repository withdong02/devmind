package com.devmind.api.controller;

import com.devmind.core.harness.Hook;
import com.devmind.harness.entity.AuditLogEntity;
import com.devmind.harness.engine.HookExecutor;
import com.devmind.harness.profile.ProfileConfig;
import com.devmind.harness.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "Harness", description = "Hook system, profiles, guardrails, audit logging")
@RestController
@RequestMapping("/api/v1/harness")
public class HarnessController {

    private final HookExecutor hookExecutor;
    private final ProfileConfig profileConfig;
    private final AuditService auditService;

    public HarnessController(HookExecutor hookExecutor, ProfileConfig profileConfig, AuditService auditService) {
        this.hookExecutor = hookExecutor;
        this.profileConfig = profileConfig;
        this.auditService = auditService;
    }

    @Operation(summary = "List all registered hooks by type")
    @GetMapping("/hooks")
    public ResponseEntity<Map<String, Object>> getHooks() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (var entry : hookExecutor.getHookChains().entrySet()) {
            List<Map<String, Object>> hooks = new ArrayList<>();
            for (Hook h : entry.getValue()) {
                hooks.add(Map.of("name", h.getName(), "order", h.getOrder()));
            }
            result.put(entry.getKey().toString(), hooks);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get all harness profiles and active profile")
    @GetMapping("/profiles")
    public ResponseEntity<Map<String, Object>> getProfiles() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("active", profileConfig.getActiveProfileName());
        result.put("profiles", profileConfig.getAllProfiles());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Activate a harness profile")
    @PostMapping("/profiles/{name}/activate")
    public ResponseEntity<Map<String, String>> activateProfile(@PathVariable String name) {
        profileConfig.setActiveProfile(name);
        return ResponseEntity.ok(Map.of("active", name, "status", "activated"));
    }

    @Operation(summary = "Query audit logs with pagination")
    @GetMapping("/audit")
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogEntity> logs = auditService.getLogs(userId, page, size);
        List<Map<String, Object>> items = new ArrayList<>();
        for (AuditLogEntity log : logs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", log.getId());
            item.put("userId", log.getUserId());
            item.put("action", log.getAction());
            item.put("actor", log.getActor());
            item.put("target", log.getTarget());
            item.put("success", log.isSuccess());
            item.put("error", log.getError());
            item.put("createdAt", log.getCreatedAt().toString());
            items.add(item);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("logs", items);
        result.put("total", logs.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get audit log statistics")
    @GetMapping("/audit/stats")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        return ResponseEntity.ok(Map.of(
                "totalLogs", auditService.count(null)
        ));
    }
}
