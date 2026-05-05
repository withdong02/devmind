package com.devmind.mcp.client.discovery;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing connections to external MCP servers.
 * Stores server configurations and their discovered tools.
 */
public class McpServerRegistry {

    private final Map<String, McpServerInfo> servers = new ConcurrentHashMap<>();

    /**
     * Registers an external MCP server.
     */
    public void registerServer(String id, String name, String transport, String endpoint) {
        servers.put(id, new McpServerInfo(id, name, transport, endpoint, List.of()));
    }

    /**
     * Returns all registered servers.
     */
    public List<McpServerInfo> getAllServers() {
        return List.copyOf(servers.values());
    }

    /**
     * Finds a server by ID.
     */
    public Optional<McpServerInfo> getServer(String id) {
        return Optional.ofNullable(servers.get(id));
    }

    /**
     * Removes a server from the registry.
     */
    public void removeServer(String id) {
        servers.remove(id);
    }

    public record McpServerInfo(
            String id,
            String name,
            String transport,
            String endpoint,
            List<String> discoveredTools
    ) {}
}
