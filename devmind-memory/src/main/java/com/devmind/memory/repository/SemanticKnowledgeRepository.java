package com.devmind.memory.repository;

import com.devmind.memory.entity.SemanticKnowledgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SemanticKnowledgeRepository extends JpaRepository<SemanticKnowledgeEntity, String> {

    List<SemanticKnowledgeEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query(value = "SELECT sk.*, 1 - (sk.embedding <=> CAST(:embedding AS vector)) AS score " +
            "FROM semantic_knowledge sk WHERE sk.user_id = :userId AND sk.embedding IS NOT NULL " +
            "ORDER BY sk.embedding <=> CAST(:embedding AS vector) LIMIT :limit", nativeQuery = true)
    List<SemanticKnowledgeEntity> findByEmbeddingSimilarity(
            @Param("userId") String userId,
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    @Modifying
    @Query(value = "UPDATE semantic_knowledge SET embedding = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
    int updateEmbedding(@Param("id") String id, @Param("embedding") String embedding);

    @Modifying
    @Query(value = "UPDATE semantic_knowledge SET access_count = access_count + 1, last_accessed = NOW() WHERE id = :id", nativeQuery = true)
    int incrementAccessCount(@Param("id") String id);

    long countByUserId(String userId);
}
