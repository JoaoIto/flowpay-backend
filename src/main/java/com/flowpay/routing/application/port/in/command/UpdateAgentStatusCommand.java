package com.flowpay.routing.application.port.in.command;

import com.flowpay.routing.domain.model.AgentStatus;
import java.util.UUID;

public record UpdateAgentStatusCommand(
    UUID agentId,
    AgentStatus status
) {}
