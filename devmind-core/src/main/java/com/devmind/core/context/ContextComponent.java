package com.devmind.core.context;

/**
 * A piece of context with priority and token cost, used by the ContextAssembler.
 */
public record ContextComponent(
    String id,
    ContextType type,
    String content,
    int tokenCount,
    double priorityScore
) {
    public enum ContextType {
        SYSTEM_PROMPT,
        USER_PREFERENCE,
        EPISODIC_MEMORY,
        SEMANTIC_KNOWLEDGE,
        RAG_DOCUMENT,
        CONVERSATION_HISTORY,
        CURRENT_QUERY
    }
}
