package com.flowpay.routing.application.port.in.command;

import com.flowpay.routing.domain.model.TeamType;

public record CreateTeamCommand(
    String name,
    TeamType type
) {}
