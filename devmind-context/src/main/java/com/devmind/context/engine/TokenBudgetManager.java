package com.devmind.context.engine;

import com.devmind.core.context.ContextComponent;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Manages token budget allocation across context types.
 * Default budget: 4096 tokens total.
 */
@Component
public class TokenBudgetManager {

    private static final int DEFAULT_TOTAL_BUDGET = 4096;

    // Default allocation ratios per context type
    private static final Map<ContextComponent.ContextType, Double> DEFAULT_RATIOS = new EnumMap<>(ContextComponent.ContextType.class);

    static {
        DEFAULT_RATIOS.put(ContextComponent.ContextType.SYSTEM_PROMPT, 0.25);          // ~1024
        DEFAULT_RATIOS.put(ContextComponent.ContextType.USER_PREFERENCE, 0.05);         // ~200
        DEFAULT_RATIOS.put(ContextComponent.ContextType.EPISODIC_MEMORY, 0.10);         // ~400
        DEFAULT_RATIOS.put(ContextComponent.ContextType.SEMANTIC_KNOWLEDGE, 0.10);      // ~400
        DEFAULT_RATIOS.put(ContextComponent.ContextType.RAG_DOCUMENT, 0.30);            // ~1200
        DEFAULT_RATIOS.put(ContextComponent.ContextType.CONVERSATION_HISTORY, 0.15);    // ~600
        DEFAULT_RATIOS.put(ContextComponent.ContextType.CURRENT_QUERY, 0.05);           // ~200
    }

    private int totalBudget;

    public TokenBudgetManager() {
        this.totalBudget = DEFAULT_TOTAL_BUDGET;
    }

    public TokenBudgetManager(int totalBudget) {
        this.totalBudget = totalBudget;
    }

    public int getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(int totalBudget) {
        this.totalBudget = totalBudget;
    }

    public int getAllocatedTokens(ContextComponent.ContextType type) {
        double ratio = DEFAULT_RATIOS.getOrDefault(type, 0.05);
        return (int) (totalBudget * ratio);
    }

    public Map<ContextComponent.ContextType, Integer> getAllocations() {
        Map<ContextComponent.ContextType, Integer> allocations = new EnumMap<>(ContextComponent.ContextType.class);
        for (ContextComponent.ContextType type : ContextComponent.ContextType.values()) {
            allocations.put(type, getAllocatedTokens(type));
        }
        return allocations;
    }

    /**
     * Estimate token count from text. Rough heuristic: ~4 chars per token for English, ~2 for CJK.
     */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int asciiCount = 0;
        int cjkCount = 0;
        for (char c : text.toCharArray()) {
            if (c > 0x7F) cjkCount++;
            else asciiCount++;
        }
        return (asciiCount / 4) + (cjkCount / 2) + 1;
    }
}
