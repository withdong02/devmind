package com.devmind.api.websocket;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        ObjectNode event = objectMapper.createObjectNode();
        event.put("type", "connection.established");
        event.put("sessionId", session.getId());
        try {
            session.sendMessage(new TextMessage(event.toString()));
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String type = node.get("type").asText();

            switch (type) {
                case "chat.send" -> handleChat(session, node);
                case "chat.cancel" -> handleCancel(session, node);
                default -> sendError(session, "Unknown event type: " + type);
            }
        } catch (Exception e) {
            sendError(session, "Failed to process message: " + e.getMessage());
        }
    }

    private void handleChat(WebSocketSession session, JsonNode node) {
        String content = node.get("content").asText();
        String sessionId = node.has("sessionId") ? node.get("sessionId").asText() : session.getId();

        // Send stream start
        sendEvent(session, "chat.stream.start", objectMapper.createObjectNode()
                .put("sessionId", sessionId));

        try {
            // Use streaming for real-time response
            chatClient.prompt()
                    .user(content)
                    .stream()
                    .content()
                    .doOnNext(chunk -> sendEvent(session, "chat.stream.chunk",
                            objectMapper.createObjectNode()
                                    .put("sessionId", sessionId)
                                    .put("content", chunk)))
                    .doOnComplete(() -> sendEvent(session, "chat.stream.end",
                            objectMapper.createObjectNode()
                                    .put("sessionId", sessionId)))
                    .doOnError(e -> sendError(session, "Stream error: " + e.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            sendError(session, "Chat error: " + e.getMessage());
        }
    }

    private void handleCancel(WebSocketSession session, JsonNode node) {
        // Cancel handling logic (to be implemented with proper session management)
        sendEvent(session, "chat.cancelled", objectMapper.createObjectNode());
    }

    private void sendEvent(WebSocketSession session, String type, JsonNode data) {
        try {
            ObjectNode event = objectMapper.createObjectNode();
            event.put("type", type);
            event.set("data", data);
            session.sendMessage(new TextMessage(event.toString()));
        } catch (Exception e) {
            // ignore send errors
        }
    }

    private void sendError(WebSocketSession session, String message) {
        sendEvent(session, "error", objectMapper.createObjectNode().put("message", message));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Cleanup
    }
}
