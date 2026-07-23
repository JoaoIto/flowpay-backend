package com.flowpay.routing.application.dto.command;

import java.util.UUID;

public record UpdateTeamCommand(
    UUID id,
    String name
) {
    public UpdateTeamCommand {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
