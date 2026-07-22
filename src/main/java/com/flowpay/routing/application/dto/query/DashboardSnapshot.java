package com.flowpay.routing.application.dto.query;

import java.time.Instant;
import java.util.List;

public record DashboardSnapshot(
    Instant timestamp,
    GlobalMetrics globalMetrics,
    List<TeamMetricsResult> teamMetrics
) {}
