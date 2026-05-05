package com.devmind.agents.agent;

import com.devmind.core.agent.AgentRole;
import com.devmind.core.rag.RetrievalResult;
import com.devmind.rag.retriever.HybridRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResearchAgent extends AbstractAgent {

    private final HybridRetriever hybridRetriever;

    public ResearchAgent(ChatClient.Builder chatClientBuilder, HybridRetriever hybridRetriever) {
        super(chatClientBuilder);
        this.hybridRetriever = hybridRetriever;
    }

    @Override
    public String getId() {
        return "research-agent";
    }

    @Override
    public AgentRole getRole() {
        return AgentRole.RESEARCH;
    }

    @Override
    public String getDescription() {
        return "Research specialist: documentation lookup, knowledge search, best practices";
    }

    @Override
    protected String getSystemPrompt() {
        return """
                You are the Research Agent in DevMind, a knowledge and documentation specialist.
                Your expertise: finding relevant documentation, searching codebases, explaining concepts.

                Guidelines:
                - Search the codebase and knowledge base for relevant information
                - Provide comprehensive answers with references to source material
                - Explain technical concepts clearly with examples
                - When uncertain, explicitly state what you know vs. what you're inferring
                - Cite specific files, functions, or documentation when possible
                """;
    }

    @Override
    protected String buildUserPrompt(com.devmind.core.agent.AgentMessage message, com.devmind.core.agent.AgentSession session) {
        // Augment with RAG results
        StringBuilder sb = new StringBuilder();
        try {
            List<RetrievalResult> results = hybridRetriever.retrieve(message.content(), 3);
            if (!results.isEmpty()) {
                sb.append("Relevant code/documents found:\n");
                for (RetrievalResult r : results) {
                    sb.append("- [").append(r.chunk().getChunkType());
                    if (r.chunk().getSymbolName() != null) {
                        sb.append(" ").append(r.chunk().getSymbolName());
                    }
                    sb.append("] ").append(r.chunk().getContent(), 0, Math.min(200, r.chunk().getContent().length()));
                    sb.append("\n");
                }
                sb.append("\n");
            }
        } catch (Exception e) {
            // RAG not available - proceed without
        }
        sb.append(super.buildUserPrompt(message, session));
        return sb.toString();
    }
}
