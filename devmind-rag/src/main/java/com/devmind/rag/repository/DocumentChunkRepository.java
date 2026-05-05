package com.devmind.rag.repository;

import com.devmind.rag.entity.DocumentChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunkEntity, String> {

    List<DocumentChunkEntity> findByDocumentIdOrderByChunkIndex(String documentId);

    @Query(value = """
        SELECT dc.*, 1 - (dc.embedding <=> CAST(:queryEmbedding AS vector)) AS score
        FROM document_chunks dc
        WHERE dc.embedding IS NOT NULL
        ORDER BY dc.embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<DocumentChunkEntity> findByEmbeddingSimilarity(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit);

    @Query(value = """
        SELECT dc.*, ts_rank(dc.search_vector, query) AS score
        FROM document_chunks dc, plainto_tsquery('english', :query) query
        WHERE dc.search_vector @@ query
        ORDER BY score DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<DocumentChunkEntity> findByFullTextSearch(
            @Param("query") String query,
            @Param("limit") int limit);

    @Modifying
    @Query(value = """
        UPDATE document_chunks
        SET search_vector = to_tsvector('english', coalesce(content, ''))
        WHERE search_vector IS NULL
        """, nativeQuery = true)
    int updateSearchVectors();

    @Modifying
    @Query(value = """
        UPDATE document_chunks
        SET embedding = CAST(:embedding AS vector)
        WHERE id = :id
        """, nativeQuery = true)
    int updateEmbedding(@Param("id") String id, @Param("embedding") String embedding);

    void deleteByDocumentId(String documentId);
}
