package com.flowpay.routing.application.dto.command;

import com.flowpay.routing.domain.model.TeamType;

public record CreateTeamCommand(String name, TeamType type) {
    public CreateTeamCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
    }
}
