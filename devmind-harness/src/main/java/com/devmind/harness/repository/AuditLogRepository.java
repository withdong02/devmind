package com.devmind.harness.repository;

import com.devmind.harness.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {
    Page<AuditLogEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Page<AuditLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByUserId(String userId);
}
