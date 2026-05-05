package com.devmind.rag.repository;

import com.devmind.rag.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    Optional<DocumentEntity> findBySourcePath(String sourcePath);

    Optional<DocumentEntity> findByFileHash(String fileHash);

    boolean existsByFileHash(String fileHash);
}
