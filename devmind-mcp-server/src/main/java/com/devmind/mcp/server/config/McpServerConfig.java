package com.devmind.mcp.server.config;

import com.devmind.core.skill.SkillRegistry;
import com.devmind.mcp.server.tools.DevMindToolProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server configuration.
 * Exposes DevMind skills as MCP tools that can be used by Claude Desktop, Cursor, etc.
 */
@Configuration
public class McpServerConfig {

    @Bean
    public DevMindToolProvider devMindToolProvider(SkillRegistry skillRegistry) {
        return new DevMindToolProvider(skillRegistry);
    }
}
