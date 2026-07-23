package com.flowpay.routing.application.dto.command;

import java.util.UUID;

public record UpdateAgentCommand(
    UUID id,
    String name,
    int maxChats
) {
    public UpdateAgentCommand {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (maxChats < 1) {
            throw new IllegalArgumentException("maxChats must be at least 1");
        }
    }
}
