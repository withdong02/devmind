package com.devmind.common.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
    UUID id,
    String title,
    Instant createdAt
) {}
