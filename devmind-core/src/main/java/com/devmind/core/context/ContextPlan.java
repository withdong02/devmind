package com.devmind.core.context;

import java.util.List;

/**
 * An ordered list of context components that fit within the token budget.
 */
public record ContextPlan(
    List<ContextComponent> components,
    int totalTokens,
    int tokenBudget
) {
    public boolean isWithinBudget() {
        return totalTokens <= tokenBudget;
    }
}
