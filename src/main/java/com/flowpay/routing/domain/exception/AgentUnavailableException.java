package com.flowpay.routing.domain.exception;

import java.util.UUID;

public class AgentUnavailableException extends RuntimeException {

    private final UUID agentId;
    private final String currentStatus;

    public AgentUnavailableException(UUID agentId, Enum<?> currentStatus) {
        super(String.format(
            "Agent %s is not available for chat assignment. Current status: %s",
            agentId, currentStatus.name()
        ));
        this.agentId = agentId;
        this.currentStatus = currentStatus.name();
    }

    public UUID getAgentId() {
        return agentId;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }
}
