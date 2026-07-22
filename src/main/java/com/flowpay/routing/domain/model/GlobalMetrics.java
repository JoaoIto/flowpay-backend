package com.flowpay.routing.domain.model;

public record GlobalMetrics(
    int totalActiveChats,
    int totalQueuedChats,
    int totalAvailableAgents,
    int totalLoggedInAgents,
    double occupancyRatePercent,
    double averageWaitTimeSeconds,
    double slaCompliancePercent,
    double abandonRatePercent
) {}
