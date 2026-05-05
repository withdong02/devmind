package com.devmind.api.controller;

import com.devmind.agents.orchestration.OrchestratorAgent;
import com.devmind.common.dto.ChatRequest;
import com.devmind.common.dto.ChatResponse;
import com.devmind.common.dto.SessionResponse;
import com.devmind.core.agent.*;
import com.devmind.core.skill.*;
import com.devmind.context.service.ContextService;
import com.devmind.memory.service.MemoryManager;
import com.devmind.skills.engine.SkillRouter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Tag(name = "Chat", description = "Chat session and message management")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private static final String DEFAULT_USER_ID = "user";
    private static final int CONSOLIDATION_INTERVAL = 10;

    private final ChatClient chatClient;
    private final SkillRouter skillRouter;
    private final MemoryManager memoryManager;
    private final ContextService contextService;
    private final OrchestratorAgent orchestrator;
    private final Map<UUID, SessionInfo> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> sessionMessages = new ConcurrentHashMap<>();

    public ChatController(ChatClient.Builder chatClientBuilder, SkillRouter skillRouter,
                          MemoryManager memoryManager, ContextService contextService,
                          OrchestratorAgent orchestrator) {
        this.chatClient = chatClientBuilder.build();
        this.skillRouter = skillRouter;
        this.memoryManager = memoryManager;
        this.contextService = contextService;
        this.orchestrator = orchestrator;
    }

    @Operation(summary = "Create a new chat session")
    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> createSession() {
        UUID sessionId = UUID.randomUUID();
        sessions.put(sessionId, new SessionInfo(sessionId, Instant.now()));
        return ResponseEntity.ok(new SessionResponse(sessionId, "New Chat", Instant.now()));
    }

    @Operation(summary = "Send a message to a chat session")
    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ChatResponse> sendMessage(
            @PathVariable UUID id,
            @RequestBody ChatRequest request) {

        String userMessage = request.content();
        String agentMode = request.agent() != null ? request.agent() : "auto";

        recordMessage(id, DEFAULT_USER_ID, "User", userMessage);

        String response = switch (agentMode) {
            case "orchestrator" -> handleOrchestration(userMessage);
            default -> handleWithSkillOrChat(id, userMessage);
        };

        recordMessage(id, DEFAULT_USER_ID, "Assistant", response);
        consolidateIfNeeded(id, DEFAULT_USER_ID);

        return ResponseEntity.ok(ChatResponse.of(UUID.randomUUID().toString(), response));
    }

    @Operation(summary = "Stream chat response via SSE")
    @GetMapping(value = "/sessions/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @PathVariable UUID id,
            @RequestParam String message) {

        recordMessage(id, DEFAULT_USER_ID, "User", message);

        var matchedSkill = skillRouter.route(message);
        if (matchedSkill.isPresent()) {
            SkillOutput output = executeSkill(matchedSkill.get(), message, id);
            recordMessage(id, DEFAULT_USER_ID, "Assistant", output.content());
            return Flux.just(output.content());
        }

        String prompt = contextService.buildAndRender(DEFAULT_USER_ID, message,
                sessionMessages.getOrDefault(id, List.of()));

        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    private String handleOrchestration(String userMessage) {
        AgentMessage agentMsg = AgentMessage.request("user", OrchestratorAgent.ID, userMessage);
        AgentSession agentSession = AgentSession.create(OrchestratorAgent.ID);
        AgentResponse agentResponse = orchestrator.handle(agentMsg, agentSession);
        return agentResponse.content();
    }

    private String handleWithSkillOrChat(UUID sessionId, String userMessage) {
        var matchedSkill = skillRouter.route(userMessage);
        if (matchedSkill.isPresent()) {
            SkillOutput output = executeSkill(matchedSkill.get(), userMessage, sessionId);
            return output.content();
        }

        String prompt = contextService.buildAndRender(DEFAULT_USER_ID, userMessage,
                sessionMessages.getOrDefault(sessionId, List.of()));
        return chatClient.prompt().user(prompt).call().content();
    }

    private SkillOutput executeSkill(Skill skill, String userMessage, UUID sessionId) {
        Map<String, Object> params = new HashMap<>();
        for (String key : skill.definition().inputSchema().keySet()) {
            params.put(key, userMessage);
        }
        SkillInput skillInput = SkillInput.of(params);
        SkillContext context = SkillContext.of(sessionId.toString(), DEFAULT_USER_ID);
        return skill.execute(skillInput, context);
    }

    private void recordMessage(UUID sessionId, String userId, String role, String content) {
        memoryManager.storeShortTerm(userId, role + ": " + content);
        sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(role + ": " + content);
    }

    private void consolidateIfNeeded(UUID sessionId, String userId) {
        List<String> messages = sessionMessages.get(sessionId);
        if (messages != null && messages.size() % CONSOLIDATION_INTERVAL == 0) {
            memoryManager.consolidateSession(userId, messages);
        }
    }

    private record SessionInfo(UUID id, Instant createdAt) {}
}
