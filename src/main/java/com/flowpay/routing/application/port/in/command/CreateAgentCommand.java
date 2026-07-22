package com.flowpay.routing.application.port.in.command;

import java.util.UUID;

public record CreateAgentCommand(
    UUID teamId,
    String name,
    String email,
    int maxConcurrentChats
) {}
