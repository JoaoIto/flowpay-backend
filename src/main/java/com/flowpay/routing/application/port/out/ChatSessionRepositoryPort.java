package com.flowpay.routing.application.port.out;

import com.flowpay.routing.domain.model.ChatSession;
import com.flowpay.routing.domain.model.ChatStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepositoryPort {
    ChatSession save(ChatSession chat);
    Optional<ChatSession> findById(UUID id);
    List<ChatSession> findByTeamIdAndStatus(UUID teamId, ChatStatus status);
    List<ChatSession> findByAgentIdAndStatus(UUID agentId, ChatStatus status);
    List<ChatSession> findByStatus(ChatStatus status);
    long countByStatus(ChatStatus status);
    long countByTeamIdAndStatus(UUID teamId, ChatStatus status);
    double calculateAverageWaitTimeSeconds(UUID teamId);
    long countChatsWithWaitTimeUnder(UUID teamId, int maxWaitSeconds);
    long countAbandonedChats(UUID teamId);
}
