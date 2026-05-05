package com.devmind.api.controller;

import com.devmind.mcp.client.discovery.McpServerRegistry;
import com.devmind.mcp.client.discovery.ToolDiscoveryService;
import com.devmind.mcp.server.tools.DevMindToolProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "MCP", description = "Model Context Protocol: server tools, client connections")
@RestController
@RequestMapping("/api/v1/mcp")
public class McpController {

    private final DevMindToolProvider toolProvider;
    private final McpServerRegistry serverRegistry;
    private final ToolDiscoveryService discoveryService;

    public McpController(
            DevMindToolProvider toolProvider,
            McpServerRegistry serverRegistry,
            ToolDiscoveryService discoveryService) {
        this.toolProvider = toolProvider;
        this.serverRegistry = serverRegistry;
        this.discoveryService = discoveryService;
    }

    @Operation(summary = "List all MCP server tools")
    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, Object>>> listTools() {
        List<Map<String, Object>> tools = toolProvider.getToolSpecifications().stream()
                .map(spec -> Map.<String, Object>of(
                        "name", spec.tool().name(),
                        "description", spec.tool().description()
                ))
                .toList();
        return ResponseEntity.ok(tools);
    }

    @Operation(summary = "List all connected MCP servers")
    @GetMapping("/servers")
    public ResponseEntity<List<McpServerRegistry.McpServerInfo>> listServers() {
        return ResponseEntity.ok(serverRegistry.getAllServers());
    }

    @Operation(summary = "Register a new MCP server")
    @PostMapping("/servers")
    public ResponseEntity<Map<String, String>> addServer(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String name = body.get("name");
        String transport = body.getOrDefault("transport", "stdio");
        String endpoint = body.get("endpoint");

        if (id == null || name == null || endpoint == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "id, name, and endpoint are required"));
        }

        serverRegistry.registerServer(id, name, transport, endpoint);
        return ResponseEntity.ok(Map.of("status", "registered", "id", id));
    }

    @Operation(summary = "Remove an MCP server")
    @DeleteMapping("/servers/{id}")
    public ResponseEntity<Map<String, String>> removeServer(@PathVariable String id) {
        serverRegistry.removeServer(id);
        return ResponseEntity.ok(Map.of("status", "removed", "id", id));
    }

    @Operation(summary = "Discover tools from an MCP server")
    @GetMapping("/servers/{id}/tools")
    public ResponseEntity<List<ToolDiscoveryService.DiscoveredTool>> discoverTools(@PathVariable String id) {
        return serverRegistry.getServer(id)
                .map(server -> ResponseEntity.ok(discoveryService.discoverTools(server)))
                .orElse(ResponseEntity.notFound().build());
    }
}
