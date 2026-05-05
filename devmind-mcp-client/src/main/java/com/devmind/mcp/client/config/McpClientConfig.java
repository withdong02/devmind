package com.devmind.mcp.client.config;

import com.devmind.mcp.client.discovery.McpServerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Client configuration.
 * Manages connections to external MCP servers.
 */
@Configuration
public class McpClientConfig {

    @Bean
    public McpServerRegistry mcpServerRegistry() {
        return new McpServerRegistry();
    }
}
