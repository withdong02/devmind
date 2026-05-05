package com.devmind.agents.service;

import com.devmind.core.agent.AgentMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

/**
 * In-process message bus for agent communication.
 * Agents subscribe to their ID and receive messages sent to them.
 */
@Component
public class AgentMessageBus {

    private static final int MAX_LOG_SIZE = 1000;

    private final Map<String, List<Consumer<AgentMessage>>> subscribers = new ConcurrentHashMap<>();
    private final LinkedBlockingDeque<AgentMessage> messageLog = new LinkedBlockingDeque<>(MAX_LOG_SIZE);

    public void subscribe(String agentId, Consumer<AgentMessage> handler) {
        subscribers.computeIfAbsent(agentId, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(handler);
    }

    public void send(AgentMessage message) {
        if (!messageLog.offer(message)) {
            messageLog.pollFirst();
            messageLog.offer(message);
        }
        List<Consumer<AgentMessage>> handlers = subscribers.get(message.toAgentId());
        if (handlers != null) {
            for (Consumer<AgentMessage> handler : handlers) {
                handler.accept(message);
            }
        }
    }

    public List<AgentMessage> getMessageLog() {
        return new ArrayList<>(messageLog);
    }

    public List<AgentMessage> getMessagesForSession(String sessionId) {
        return new ArrayList<>(messageLog);
    }

    public void clearLog() {
        messageLog.clear();
    }
}
