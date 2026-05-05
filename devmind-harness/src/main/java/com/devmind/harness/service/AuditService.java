package com.devmind.harness.service;

import com.devmind.harness.entity.AuditLogEntity;
import com.devmind.harness.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(String userId, String action, String actor, String target,
                    String inputData, String outputData, boolean success, String error) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setUserId(userId);
        entity.setAction(action);
        entity.setActor(actor);
        entity.setTarget(target);
        entity.setInputData(inputData);
        entity.setOutputData(outputData);
        entity.setSuccess(success);
        entity.setError(error);
        repository.save(entity);
    }

    public void logSuccess(String userId, String action, String actor, String target, String input) {
        log(userId, action, actor, target, input, null, true, null);
    }

    public void logFailure(String userId, String action, String actor, String target, String input, String error) {
        log(userId, action, actor, target, input, null, false, error);
    }

    public Page<AuditLogEntity> getLogs(String userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (userId != null) {
            return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public long count(String userId) {
        return repository.countByUserId(userId);
    }
}
