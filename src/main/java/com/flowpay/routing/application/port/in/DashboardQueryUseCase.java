package com.flowpay.routing.application.port.in;

import com.flowpay.routing.application.dto.query.DashboardSnapshot;
import com.flowpay.routing.application.dto.query.TeamMetricsResult;
import java.util.UUID;

public interface DashboardQueryUseCase {
    DashboardSnapshot getDashboardSnapshot();
    TeamMetricsResult getTeamMetrics(UUID teamId);
}
