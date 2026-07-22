package com.flowpay.routing.application.dto.command;

import com.flowpay.routing.domain.model.TeamType;

public record RouteChatCommand(
    String customerId,
    TeamType teamType,
    String channel,
    String subject
) {
    public RouteChatCommand {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (teamType == null) {
            throw new IllegalArgumentException("teamType must not be null");
        }
        if (channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("channel must not be blank");
        }
    }
}
