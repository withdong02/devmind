package com.devmind.core.memory;

import java.util.List;

/**
 * Core interface for all memory types.
 */
public interface Memory {

    /**
     * Stores a memory entry.
     */
    void store(MemoryEntry entry);

    /**
     * Retrieves relevant memories based on a query.
     */
    List<MemoryEntry> retrieve(MemoryQuery query);

    /**
     * Forgets/removes a specific memory.
     */
    void forget(String memoryId);

    /**
     * Returns the type of this memory.
     */
    MemoryType getType();
}
