package com.flowpay.routing.application.dto.command;

import com.flowpay.routing.domain.model.AgentStatus;
import java.util.UUID;

public record UpdateAgentStatusCommand(UUID agentId, AgentStatus newStatus) {
    public UpdateAgentStatusCommand {
        if (agentId == null) {
            throw new IllegalArgumentException("agentId must not be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }
    }
}
