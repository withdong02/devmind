package com.devmind.common.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank String content,
    String agent
) {
    public ChatRequest {
        // agent is optional, defaults to null
    }
}
