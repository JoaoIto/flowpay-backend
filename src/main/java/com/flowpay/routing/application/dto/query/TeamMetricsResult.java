package com.flowpay.routing.application.dto.query;

import java.util.UUID;

public record TeamMetricsResult(
    UUID teamId,
    String teamName,
    int activeChats,
    int queuedChats,
    int availableAgents,
    int totalAgents,
    double occupancyRatePercent,
    double averageWaitTimeSeconds
) {}
