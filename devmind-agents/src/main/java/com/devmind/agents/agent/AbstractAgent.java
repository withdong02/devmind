package com.devmind.agents.agent;

import com.devmind.core.agent.*;
import org.springframework.ai.chat.client.ChatClient;

/**
 * Base class for all specialist agents. Provides LLM interaction and common utilities.
 */
public abstract class AbstractAgent implements Agent {

    protected final ChatClient chatClient;

    protected AbstractAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public AgentResponse handle(AgentMessage message, AgentSession session) {
        session.addMessage(message);

        String systemPrompt = getSystemPrompt();
        String userPrompt = buildUserPrompt(message, session);

        try {
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            AgentResponse reply = AgentResponse.reply(getId(), response);
            session.addMessage(AgentMessage.response(getId(), message.fromAgentId(), response));
            return reply;
        } catch (Exception e) {
            return AgentResponse.reply(getId(), "Error processing request: " + e.getMessage());
        }
    }

    protected abstract String getSystemPrompt();

    protected String buildUserPrompt(AgentMessage message, AgentSession session) {
        StringBuilder sb = new StringBuilder();
        // Include recent history for context
        var history = session.getHistory();
        int start = Math.max(0, history.size() - 5);
        for (int i = start; i < history.size(); i++) {
            AgentMessage msg = history.get(i);
            sb.append("[").append(msg.type()).append("] ").append(msg.content()).append("\n");
        }
        sb.append("\nCurrent request: ").append(message.content());
        return sb.toString();
    }
}
