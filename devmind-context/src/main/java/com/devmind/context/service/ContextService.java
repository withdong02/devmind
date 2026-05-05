package com.devmind.context.service;

import com.devmind.core.context.ContextComponent;
import com.devmind.core.context.ContextPlan;
import com.devmind.core.memory.MemoryEntry;
import com.devmind.core.memory.MemoryType;
import com.devmind.core.rag.RetrievalResult;
import com.devmind.context.engine.ContextAssembler;
import com.devmind.context.engine.ContextPrioritizer;
import com.devmind.context.engine.TokenBudgetManager;
import com.devmind.context.template.PromptTemplateEngine;
import com.devmind.memory.service.MemoryManager;
import com.devmind.rag.retriever.HybridRetriever;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Orchestrates context assembly: gathers components from memory, RAG, conversation,
 * prioritizes them, and assembles optimal context within token budget.
 */
@Service
public class ContextService {

    private final MemoryManager memoryManager;
    private final HybridRetriever hybridRetriever;
    private final TokenBudgetManager budgetManager;
    private final ContextPrioritizer prioritizer;
    private final ContextAssembler assembler;
    private final PromptTemplateEngine templateEngine;

    public ContextService(MemoryManager memoryManager,
                          HybridRetriever hybridRetriever,
                          TokenBudgetManager budgetManager,
                          ContextPrioritizer prioritizer,
                          ContextAssembler assembler,
                          PromptTemplateEngine templateEngine) {
        this.memoryManager = memoryManager;
        this.hybridRetriever = hybridRetriever;
        this.budgetManager = budgetManager;
        this.prioritizer = prioritizer;
        this.assembler = assembler;
        this.templateEngine = templateEngine;
    }

    /**
     * Build optimal context for a user query.
     */
    public ContextPlan buildContext(String userId, String query, List<String> conversationHistory) {
        List<ContextAssembler.ScoredComponent> scored = new ArrayList<>();

        // 1. System prompt
        String systemPrompt = buildSystemPrompt(userId);
        scored.add(new ContextAssembler.ScoredComponent(
                new ContextComponent("system-prompt", ContextComponent.ContextType.SYSTEM_PROMPT,
                        systemPrompt, TokenBudgetManager.estimateTokens(systemPrompt), 1.0),
                1.0
        ));

        // 2. User preferences from long-term memory
        List<MemoryEntry> preferences = memoryManager.retrieveByType(userId, query, MemoryType.LONG_TERM, 3);
        for (MemoryEntry pref : preferences) {
            int tokens = TokenBudgetManager.estimateTokens(pref.getContent());
            double score = prioritizer.scoreSimple(
                    new ContextComponent(pref.getId(), ContextComponent.ContextType.USER_PREFERENCE,
                            pref.getContent(), tokens, pref.getImportance()),
                    0.5
            );
            scored.add(new ContextAssembler.ScoredComponent(
                    new ContextComponent(pref.getId(), ContextComponent.ContextType.USER_PREFERENCE,
                            pref.getContent(), tokens, pref.getImportance()),
                    score
            ));
        }

        // 3. Episodic memories
        List<MemoryEntry> episodes = memoryManager.retrieveByType(userId, query, MemoryType.EPISODIC, 3);
        for (MemoryEntry ep : episodes) {
            int tokens = TokenBudgetManager.estimateTokens(ep.getContent());
            double score = prioritizer.scoreSimple(
                    new ContextComponent(ep.getId(), ContextComponent.ContextType.EPISODIC_MEMORY,
                            ep.getContent(), tokens, ep.getImportance()),
                    0.6
            );
            scored.add(new ContextAssembler.ScoredComponent(
                    new ContextComponent(ep.getId(), ContextComponent.ContextType.EPISODIC_MEMORY,
                            formatEpisodic(ep), tokens, ep.getImportance()),
                    score
            ));
        }

        // 4. Semantic knowledge
        List<MemoryEntry> knowledge = memoryManager.retrieveByType(userId, query, MemoryType.SEMANTIC, 5);
        for (MemoryEntry kn : knowledge) {
            int tokens = TokenBudgetManager.estimateTokens(kn.getContent());
            double score = prioritizer.scoreSimple(
                    new ContextComponent(kn.getId(), ContextComponent.ContextType.SEMANTIC_KNOWLEDGE,
                            kn.getContent(), tokens, kn.getImportance()),
                    0.7
            );
            scored.add(new ContextAssembler.ScoredComponent(
                    new ContextComponent(kn.getId(), ContextComponent.ContextType.SEMANTIC_KNOWLEDGE,
                            kn.getContent(), tokens, kn.getImportance()),
                    score
            ));
        }

        // 5. RAG documents
        try {
            List<RetrievalResult> ragResults = hybridRetriever.retrieve(query, 5);
            for (RetrievalResult rr : ragResults) {
                String content = rr.chunk().getContent();
                int tokens = TokenBudgetManager.estimateTokens(content);
                String formatted = formatRagResult(rr);
                double score = prioritizer.scoreSimple(
                        new ContextComponent(rr.chunk().getId(), ContextComponent.ContextType.RAG_DOCUMENT,
                                formatted, tokens, (float) rr.score()),
                        rr.score()
                );
                scored.add(new ContextAssembler.ScoredComponent(
                        new ContextComponent(rr.chunk().getId(), ContextComponent.ContextType.RAG_DOCUMENT,
                                formatted, tokens, (float) rr.score()),
                        score
                ));
            }
        } catch (Exception e) {
            // RAG retrieval failed - skip
        }

        // 6. Conversation history (last N messages)
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            int start = Math.max(0, conversationHistory.size() - 10);
            for (int i = start; i < conversationHistory.size(); i++) {
                String msg = conversationHistory.get(i);
                int tokens = TokenBudgetManager.estimateTokens(msg);
                double timeDecay = (i - start) / (double) (conversationHistory.size() - start);
                scored.add(new ContextAssembler.ScoredComponent(
                        new ContextComponent("msg-" + i, ContextComponent.ContextType.CONVERSATION_HISTORY,
                                msg, tokens, timeDecay),
                        0.3 + timeDecay * 0.4
                ));
            }
        }

        return assembler.assemble(scored, query);
    }

    /**
     * Render a context plan as a single prompt string.
     */
    public String renderPrompt(ContextPlan plan) {
        StringBuilder sb = new StringBuilder();
        for (ContextComponent comp : plan.components()) {
            switch (comp.type()) {
                case SYSTEM_PROMPT -> sb.insert(0, comp.content() + "\n\n");
                case USER_PREFERENCE -> sb.append("[Preference] ").append(comp.content()).append("\n");
                case EPISODIC_MEMORY -> sb.append("[Past Experience] ").append(comp.content()).append("\n");
                case SEMANTIC_KNOWLEDGE -> sb.append("[Knowledge] ").append(comp.content()).append("\n");
                case RAG_DOCUMENT -> sb.append("[Reference] ").append(comp.content()).append("\n");
                case CONVERSATION_HISTORY -> sb.append(comp.content()).append("\n");
                case CURRENT_QUERY -> sb.append("\nUser: ").append(comp.content()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Build context and render as prompt in one call.
     */
    public String buildAndRender(String userId, String query, List<String> conversationHistory) {
        ContextPlan plan = buildContext(userId, query, conversationHistory);
        return renderPrompt(plan);
    }

    public TokenBudgetManager getBudgetManager() {
        return budgetManager;
    }

    private String buildSystemPrompt(String userId) {
        Map<String, String> vars = new HashMap<>();
        List<MemoryEntry> prefs = memoryManager.retrieveByType(userId, "", MemoryType.LONG_TERM, 3);
        String prefText = prefs.isEmpty() ? "No preferences recorded yet."
                : prefs.stream().map(MemoryEntry::getContent).reduce("", (a, b) -> a + "- " + b + "\n");
        vars.put("user_preferences", "User preferences:\n" + prefText);
        vars.put("relevant_knowledge", "");
        vars.put("episodic_context", "");
        return templateEngine.render("system-with-memory", vars);
    }

    private String formatEpisodic(MemoryEntry ep) {
        StringBuilder sb = new StringBuilder(ep.getContent());
        if (ep.getMetadata() != null) {
            Object outcome = ep.getMetadata().get("outcome");
            Object learnings = ep.getMetadata().get("learnings");
            if (outcome != null) sb.append(" [Outcome: ").append(outcome).append("]");
            if (learnings != null && !learnings.toString().isEmpty()) {
                sb.append(" [Learnings: ").append(learnings).append("]");
            }
        }
        return sb.toString();
    }

    private String formatRagResult(RetrievalResult result) {
        String symbol = result.chunk().getSymbolName();
        String type = result.chunk().getChunkType().toString();
        String location = result.chunk().getStartLine() > 0
                ? " (L" + result.chunk().getStartLine() + "-" + result.chunk().getEndLine() + ")"
                : "";
        String prefix = symbol != null && !symbol.isEmpty()
                ? "[" + type + " " + symbol + location + "] "
                : "[" + type + location + "] ";
        return prefix + result.chunk().getContent();
    }
}
