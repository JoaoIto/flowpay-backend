package com.flowpay.routing.application.port.out;

import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.AgentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentRepositoryPort {
    Agent save(Agent agent);
    Optional<Agent> findById(UUID id);
    List<Agent> findByTeamId(UUID teamId);
    List<Agent> findAll();
    void delete(UUID id);
    Optional<Agent> findAvailableAgentWithLock(UUID teamId);
    List<Agent> findAllByStatus(AgentStatus status);
    long countByTeamIdAndStatus(UUID teamId, AgentStatus status);
}
