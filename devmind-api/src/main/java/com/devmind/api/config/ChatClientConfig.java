package com.devmind.api.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("You are DevMind, an AI-powered developer assistant. " +
                        "You help developers with code analysis, technical Q&A, " +
                        "task decomposition, and execution. Be concise and practical.")
                .build();
    }
}
