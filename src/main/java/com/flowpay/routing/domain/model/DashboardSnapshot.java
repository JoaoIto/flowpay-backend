package com.flowpay.routing.domain.model;

import java.util.List;

public record DashboardSnapshot(
    String timestamp,
    GlobalMetrics globalMetrics,
    List<TeamMetricsResult> teamMetrics
) {}
