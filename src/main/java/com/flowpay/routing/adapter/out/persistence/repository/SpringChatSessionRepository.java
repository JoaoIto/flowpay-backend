package com.flowpay.routing.adapter.out.persistence.repository;

import com.flowpay.routing.adapter.out.persistence.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringChatSessionRepository extends JpaRepository<ChatSessionEntity, UUID> {
    List<ChatSessionEntity> findByTeamIdAndStatus(UUID teamId, String status);
    List<ChatSessionEntity> findByAgentIdAndStatus(UUID agentId, String status);
    List<ChatSessionEntity> findByStatus(String status);
    long countByStatus(String status);
    long countByTeamIdAndStatus(UUID teamId, String status);

    @Query(value = "SELECT COALESCE(EXTRACT(EPOCH FROM AVG(started_at - queued_at)), 0) FROM chat_sessions WHERE team_id = :teamId AND started_at IS NOT NULL", nativeQuery = true)
    double calculateAverageWaitTimeSeconds(@Param("teamId") UUID teamId);

    @Query(value = "SELECT COUNT(*) FROM chat_sessions WHERE team_id = :teamId AND started_at IS NOT NULL AND EXTRACT(EPOCH FROM (started_at - queued_at)) <= :maxWaitSeconds", nativeQuery = true)
    long countChatsWithWaitTimeUnder(@Param("teamId") UUID teamId, @Param("maxWaitSeconds") int maxWaitSeconds);

    @Query(value = "SELECT COUNT(*) FROM chat_sessions WHERE team_id = :teamId AND status = 'CLOSED' AND started_at IS NULL", nativeQuery = true)
    long countAbandonedChats(@Param("teamId") UUID teamId);
}
