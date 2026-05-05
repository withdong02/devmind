package com.devmind.memory.repository;

import com.devmind.memory.entity.LongTermMemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LongTermMemoryRepository extends JpaRepository<LongTermMemoryEntity, String> {

    List<LongTermMemoryEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<LongTermMemoryEntity> findByUserIdAndMemoryTypeOrderByCreatedAtDesc(String userId, String memoryType);

    @Query(value = "SELECT ltm.*, 1 - (ltm.embedding <=> CAST(:embedding AS vector)) AS score " +
            "FROM long_term_memories ltm WHERE ltm.user_id = :userId AND ltm.embedding IS NOT NULL " +
            "ORDER BY ltm.embedding <=> CAST(:embedding AS vector) LIMIT :limit", nativeQuery = true)
    List<LongTermMemoryEntity> findByEmbeddingSimilarity(
            @Param("userId") String userId,
            @Param("embedding") String embedding,
            @Param("limit") int limit);

    @Modifying
    @Query(value = "UPDATE long_term_memories SET embedding = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
    int updateEmbedding(@Param("id") String id, @Param("embedding") String embedding);

    @Modifying
    @Query(value = "UPDATE long_term_memories SET access_count = access_count + 1, last_accessed = NOW() WHERE id = :id", nativeQuery = true)
    int incrementAccessCount(@Param("id") String id);

    long countByUserId(String userId);

    void deleteByUserId(String userId);
}
