package com.devmind.rag.retriever;

import com.devmind.core.rag.RetrievalResult;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class HybridRetriever {

    private final SemanticRetriever semanticRetriever;
    private final KeywordRetriever keywordRetriever;

    private static final double SEMANTIC_WEIGHT = 0.6;
    private static final double KEYWORD_WEIGHT = 0.4;
    private static final int RRF_K = 60;

    public HybridRetriever(SemanticRetriever semanticRetriever, KeywordRetriever keywordRetriever) {
        this.semanticRetriever = semanticRetriever;
        this.keywordRetriever = keywordRetriever;
    }

    public List<RetrievalResult> retrieve(String query, int limit) {
        List<RetrievalResult> semanticResults = semanticRetriever.retrieve(query, limit * 2);
        List<RetrievalResult> keywordResults = keywordRetriever.retrieve(query, limit * 2);

        // Reciprocal Rank Fusion (RRF)
        Map<String, Double> scores = new HashMap<>();
        Map<String, RetrievalResult> resultMap = new HashMap<>();

        for (int i = 0; i < semanticResults.size(); i++) {
            String chunkId = semanticResults.get(i).chunk().getId();
            double rrfScore = SEMANTIC_WEIGHT / (RRF_K + i + 1);
            scores.merge(chunkId, rrfScore, Double::sum);
            resultMap.putIfAbsent(chunkId, semanticResults.get(i));
        }

        for (int i = 0; i < keywordResults.size(); i++) {
            String chunkId = keywordResults.get(i).chunk().getId();
            double rrfScore = KEYWORD_WEIGHT / (RRF_K + i + 1);
            scores.merge(chunkId, rrfScore, Double::sum);
            resultMap.putIfAbsent(chunkId, keywordResults.get(i));
        }

        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(limit)
            .map(e -> {
                RetrievalResult orig = resultMap.get(e.getKey());
                return new RetrievalResult(orig.chunk(), e.getValue(), "hybrid");
            })
            .collect(Collectors.toList());
    }
}
