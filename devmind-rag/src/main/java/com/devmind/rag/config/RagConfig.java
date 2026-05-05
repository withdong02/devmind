package com.devmind.rag.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.List;

@Configuration
public class RagConfig {

    /**
     * Development-mode embedding model that generates deterministic vectors from content hash.
     * In production, replace with a real embedding model (OpenAI text-embedding-3-small, Ollama, etc.).
     */
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        return new HashBasedEmbeddingModel();
    }

    static class HashBasedEmbeddingModel implements EmbeddingModel {

        private static final int DIMENSION = 384;

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<Embedding> embeddings = new java.util.ArrayList<>();
            List<String> texts = request.getInstructions();
            for (int i = 0; i < texts.size(); i++) {
                float[] vector = generateVector(texts.get(i));
                embeddings.add(new Embedding(vector, i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return generateVector(document.getText());
        }

        @Override
        public int dimensions() {
            return DIMENSION;
        }

        private float[] generateVector(String text) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                float[] vector = new float[DIMENSION];

                for (int i = 0; i < DIMENSION; i++) {
                    byte[] hash = md5.digest((i + ":" + text).getBytes());
                    int val = ByteBuffer.wrap(hash).getInt();
                    vector[i] = (val % 1000) / 1000.0f;
                }

                // Normalize to unit vector
                float norm = 0;
                for (float v : vector) norm += v * v;
                norm = (float) Math.sqrt(norm);
                if (norm > 0) {
                    for (int i = 0; i < vector.length; i++) {
                        vector[i] /= norm;
                    }
                }

                return vector;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
