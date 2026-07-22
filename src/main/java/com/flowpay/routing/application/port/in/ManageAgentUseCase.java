package com.flowpay.routing.application.port.in;

import com.flowpay.routing.application.dto.command.CreateAgentCommand;
import com.flowpay.routing.application.dto.command.UpdateAgentStatusCommand;
import com.flowpay.routing.domain.model.Agent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManageAgentUseCase {
    Agent createAgent(CreateAgentCommand command);
    Agent updateAgentStatus(UpdateAgentStatusCommand command);
    Optional<Agent> findAgentById(UUID agentId);
    List<Agent> findAgentsByTeamId(UUID teamId);
    List<Agent> findAllAgents();
}
