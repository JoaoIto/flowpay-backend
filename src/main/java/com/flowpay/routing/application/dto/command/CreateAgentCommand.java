package com.flowpay.routing.application.dto.command;

import java.util.UUID;

public record CreateAgentCommand(String name, UUID teamId) {
    public CreateAgentCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
    }
}
