package com.devmind.memory.repository;

import com.devmind.memory.entity.EpisodicMemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpisodicMemoryRepository extends JpaRepository<EpisodicMemoryEntity, String> {

    List<EpisodicMemoryEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query(value = "SELECT em.*, 1 - (em.embedding <=> CAST(:embedding AS vector)) AS score " +
            "FROM episodic_memories em WHERE em.user_id = :userId AND em.embedding IS NOT NULL " +
            "ORDER BY em.embedding <=> CAST(:embedding AS vector) LIMIT :limit", nativeQuery = true)
    List<EpisodicMemoryEntity> findByEmbeddingSimilarity(
            @Param("userId") String userId,
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    @Modifying
    @Query(value = "UPDATE episodic_memories SET embedding = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
    int updateEmbedding(@Param("id") String id, @Param("embedding") String embedding);

    @Modifying
    @Query(value = "UPDATE episodic_memories SET access_count = access_count + 1, last_accessed = NOW() WHERE id = :id", nativeQuery = true)
    int incrementAccessCount(@Param("id") String id);

    long countByUserId(String userId);
}
