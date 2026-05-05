package com.devmind.mcp.server.tools;

import com.devmind.core.skill.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts DevMind skills into MCP tool specifications.
 * Each skill becomes an MCP tool that external clients can discover and invoke.
 */
public class DevMindToolProvider {

    private final SkillRegistry skillRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DevMindToolProvider(SkillRegistry skillRegistry) {
        this.skillRegistry = skillRegistry;
    }

    /**
     * Returns MCP tool specifications for all registered skills.
     */
    public List<McpServerFeatures.SyncToolSpecification> getToolSpecifications() {
        List<McpServerFeatures.SyncToolSpecification> tools = new ArrayList<>();

        for (Skill skill : skillRegistry.getAllSkills()) {
            tools.add(createToolSpec(skill));
        }

        return tools;
    }

    private McpServerFeatures.SyncToolSpecification createToolSpec(Skill skill) {
        SkillDefinition def = skill.definition();

        String jsonSchema = buildJsonSchema(def.inputSchema());
        McpSchema.Tool tool = new McpSchema.Tool(def.id(), def.description(), jsonSchema);

        return new McpServerFeatures.SyncToolSpecification(
                tool,
                (McpSyncServerExchange exchange, Map<String, Object> args) -> {
                    SkillInput input = SkillInput.of(args);
                    SkillContext context = SkillContext.of("mcp-external", "mcp-client");
                    SkillOutput output = skill.execute(input, context);

                    McpSchema.TextContent textContent = new McpSchema.TextContent(output.content());
                    return new McpSchema.CallToolResult(List.of(textContent), !output.success());
                }
        );
    }

    private String buildJsonSchema(Map<String, String> inputSchema) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        inputSchema.forEach((key, type) -> {
            properties.put(key, Map.of("type", type, "description", "The " + key + " input"));
            required.add(key);
        });

        schema.put("properties", properties);
        schema.put("required", required);

        try {
            return objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
