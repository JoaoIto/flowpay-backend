package com.flowpay.routing.domain.model;

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
