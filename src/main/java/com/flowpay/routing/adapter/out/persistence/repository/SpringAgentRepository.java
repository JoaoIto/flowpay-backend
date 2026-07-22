package com.flowpay.routing.adapter.out.persistence.repository;

import com.flowpay.routing.adapter.out.persistence.entity.AgentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringAgentRepository extends JpaRepository<AgentEntity, UUID> {
    List<AgentEntity> findByTeamId(UUID teamId);
    List<AgentEntity> findByStatus(String status);
    long countByTeamIdAndStatus(UUID teamId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AgentEntity a WHERE a.teamId = :teamId AND a.status = 'AVAILABLE' AND a.activeChatsCount < a.maxChats ORDER BY a.activeChatsCount ASC LIMIT 1")
    Optional<AgentEntity> findAvailableAgentWithLock(@Param("teamId") UUID teamId);
}
