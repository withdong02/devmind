package com.devmind.memory.impl;

import com.devmind.core.memory.Memory;
import com.devmind.core.memory.MemoryEntry;
import com.devmind.core.memory.MemoryQuery;
import com.devmind.core.memory.MemoryType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ShortTermMemory implements Memory {

    private static final String KEY_PREFIX = "stm:";
    private static final long TTL_HOURS = 24;

    private final StringRedisTemplate redisTemplate;

    public ShortTermMemory(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void store(MemoryEntry entry) {
        String key = KEY_PREFIX + entry.getUserId() + ":" + entry.getId();
        Map<String, String> hash = new HashMap<>();
        hash.put("id", entry.getId());
        hash.put("userId", entry.getUserId());
        hash.put("content", entry.getContent());
        hash.put("importance", String.valueOf(entry.getImportance()));
        hash.put("accessCount", String.valueOf(entry.getAccessCount()));
        hash.put("createdAt", entry.getCreatedAt().toString());
        if (entry.getMetadata() != null) {
            hash.put("metadata", entry.getMetadata().toString());
        }
        redisTemplate.opsForHash().putAll(key, hash);
        redisTemplate.expire(key, TTL_HOURS, TimeUnit.HOURS);

        // Add to user's index
        String indexKey = KEY_PREFIX + entry.getUserId() + ":index";
        redisTemplate.opsForSet().add(indexKey, entry.getId());
        redisTemplate.expire(indexKey, TTL_HOURS, TimeUnit.HOURS);
    }

    @Override
    public List<MemoryEntry> retrieve(MemoryQuery query) {
        String indexKey = KEY_PREFIX + query.userId() + ":index";
        Set<String> ids = redisTemplate.opsForSet().members(indexKey);
        if (ids == null || ids.isEmpty()) return List.of();

        return ids.stream()
                .map(id -> {
                    String key = KEY_PREFIX + query.userId() + ":" + id;
                    Map<Object, Object> hash = redisTemplate.opsForHash().entries(key);
                    if (hash.isEmpty()) return null;
                    MemoryEntry entry = new MemoryEntry();
                    entry.setId((String) hash.get("id"));
                    entry.setUserId((String) hash.get("userId"));
                    entry.setContent((String) hash.get("content"));
                    entry.setImportance(Float.parseFloat((String) hash.getOrDefault("importance", "0.5")));
                    entry.setAccessCount(Integer.parseInt((String) hash.getOrDefault("accessCount", "0")));
                    entry.setCreatedAt(Instant.parse((String) hash.get("createdAt")));
                    entry.setType(MemoryType.SHORT_TERM);
                    return entry;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(MemoryEntry::getCreatedAt).reversed())
                .limit(query.limit())
                .collect(Collectors.toList());
    }

    @Override
    public void forget(String memoryId) {
        // Need to scan all users - for simplicity, just delete from known keys
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*:" + memoryId);
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    public void clearSession(String userId) {
        String indexKey = KEY_PREFIX + userId + ":index";
        Set<String> ids = redisTemplate.opsForSet().members(indexKey);
        if (ids != null) {
            for (String id : ids) {
                redisTemplate.delete(KEY_PREFIX + userId + ":" + id);
            }
        }
        redisTemplate.delete(indexKey);
    }

    @Override
    public MemoryType getType() {
        return MemoryType.SHORT_TERM;
    }
}
