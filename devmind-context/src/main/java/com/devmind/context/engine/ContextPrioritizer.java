package com.devmind.context.engine;

import com.devmind.core.context.ContextComponent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scores context components using weighted factors:
 * - Semantic relevance (similarity to current query)
 * - Time decay (recent memories score higher)
 * - Importance (explicit importance from memory system)
 * - Access frequency (frequently accessed memories score higher)
 */
@Component
public class ContextPrioritizer {

    // Weight configuration
    private double semanticWeight = 0.4;
    private double timeDecayWeight = 0.2;
    private double importanceWeight = 0.3;
    private double accessWeight = 0.1;

    /**
     * Calculate priority score for a context component.
     *
     * @param component       the context component
     * @param semanticScore   similarity score (0-1) to current query
     * @param createdAt       when the memory was created
     * @param importance      explicit importance (0-1)
     * @param accessCount     how many times accessed
     */
    public double score(ContextComponent component,
                        double semanticScore,
                        Instant createdAt,
                        double importance,
                        int accessCount) {
        double timeDecay = calculateTimeDecay(createdAt);
        double accessScore = Math.min(accessCount / 10.0, 1.0); // normalize to 0-1

        return (semanticWeight * semanticScore)
                + (timeDecayWeight * timeDecay)
                + (importanceWeight * importance)
                + (accessWeight * accessScore);
    }

    /**
     * Simple scoring without external metadata - uses component's own priority score.
     */
    public double scoreSimple(ContextComponent component, double semanticScore) {
        double baseScore = component.priorityScore();
        return (semanticWeight * semanticScore) + (importanceWeight * baseScore) + 0.2;
    }

    private double calculateTimeDecay(Instant createdAt) {
        if (createdAt == null) return 0.5;
        long hoursAgo = ChronoUnit.HOURS.between(createdAt, Instant.now());
        // Exponential decay: half-life of 72 hours (3 days)
        return Math.exp(-0.693 * hoursAgo / 72.0);
    }

    public void setWeights(double semantic, double timeDecay, double importance, double access) {
        this.semanticWeight = semantic;
        this.timeDecayWeight = timeDecay;
        this.importanceWeight = importance;
        this.accessWeight = access;
    }
}
