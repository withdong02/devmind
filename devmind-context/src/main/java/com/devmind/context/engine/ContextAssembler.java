package com.devmind.context.engine;

import com.devmind.core.context.ContextComponent;
import com.devmind.core.context.ContextPlan;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Assembles optimal context within token budget using a greedy approach.
 * Prioritizes components by score, fills budget greedily.
 * Reserves minimum allocations for critical types (system prompt, current query).
 */
@Component
public class ContextAssembler {

    private final TokenBudgetManager budgetManager;

    public ContextAssembler(TokenBudgetManager budgetManager) {
        this.budgetManager = budgetManager;
    }

    /**
     * Assemble context from scored components. Each component should already have a priorityScore.
     * Uses greedy selection: sort by score/token ratio, fill until budget exhausted.
     */
    public ContextPlan assemble(List<ScoredComponent> scoredComponents, String currentQuery) {
        int budget = budgetManager.getTotalBudget();
        int queryTokens = TokenBudgetManager.estimateTokens(currentQuery);

        // Reserve space for current query (mandatory)
        int availableBudget = budget - queryTokens;
        if (availableBudget < 0) availableBudget = budget / 2;

        // Sort by score descending
        scoredComponents.sort((a, b) -> Double.compare(b.score, a.score));

        List<ContextComponent> selected = new ArrayList<>();
        int usedTokens = 0;

        // Greedy selection: pick highest-scored components that fit
        for (ScoredComponent sc : scoredComponents) {
            int componentTokens = sc.component.tokenCount();
            if (usedTokens + componentTokens <= availableBudget) {
                selected.add(sc.component);
                usedTokens += componentTokens;
            }
        }

        // Add current query as the last component
        ContextComponent queryComponent = new ContextComponent(
                "current-query",
                ContextComponent.ContextType.CURRENT_QUERY,
                currentQuery,
                queryTokens,
                1.0
        );
        selected.add(queryComponent);
        usedTokens += queryTokens;

        return new ContextPlan(selected, usedTokens, budget);
    }

    /**
     * Assemble with type-level guarantees: ensure at least minTokens for each present type.
     */
    public ContextPlan assembleWithGuarantees(List<ScoredComponent> scoredComponents,
                                               String currentQuery,
                                               Map<ContextComponent.ContextType, Integer> minTokens) {
        int budget = budgetManager.getTotalBudget();
        int queryTokens = TokenBudgetManager.estimateTokens(currentQuery);
        int availableBudget = budget - queryTokens;

        // Group by type
        Map<ContextComponent.ContextType, List<ScoredComponent>> byType = new EnumMap<>(ContextComponent.ContextType.class);
        for (ScoredComponent sc : scoredComponents) {
            byType.computeIfAbsent(sc.component.type(), k -> new ArrayList<>()).add(sc);
        }

        // Sort each type group by score
        for (var entry : byType.values()) {
            entry.sort((a, b) -> Double.compare(b.score, a.score));
        }

        List<ContextComponent> selected = new ArrayList<>();
        int usedTokens = 0;

        // Phase 1: Satisfy minimum guarantees
        for (var entry : byType.entrySet()) {
            int min = minTokens.getOrDefault(entry.getKey(), 0);
            int typeUsed = 0;
            for (ScoredComponent sc : entry.getValue()) {
                if (typeUsed >= min) break;
                int tokens = sc.component.tokenCount();
                if (usedTokens + tokens <= availableBudget) {
                    selected.add(sc.component);
                    usedTokens += tokens;
                    typeUsed += tokens;
                }
            }
        }

        // Phase 2: Fill remaining budget greedily with all remaining components
        Set<String> selectedIds = new HashSet<>();
        for (ContextComponent c : selected) selectedIds.add(c.id());

        List<ScoredComponent> remaining = new ArrayList<>();
        for (var entry : byType.entrySet()) {
            for (ScoredComponent sc : entry.getValue()) {
                if (!selectedIds.contains(sc.component.id())) {
                    remaining.add(sc);
                }
            }
        }
        remaining.sort((a, b) -> Double.compare(b.score, a.score));

        for (ScoredComponent sc : remaining) {
            int tokens = sc.component.tokenCount();
            if (usedTokens + tokens <= availableBudget) {
                selected.add(sc.component);
                usedTokens += tokens;
            }
        }

        // Add current query
        selected.add(new ContextComponent(
                "current-query", ContextComponent.ContextType.CURRENT_QUERY,
                currentQuery, queryTokens, 1.0));
        usedTokens += queryTokens;

        return new ContextPlan(selected, usedTokens, budget);
    }

    public record ScoredComponent(ContextComponent component, double score) {}
}
