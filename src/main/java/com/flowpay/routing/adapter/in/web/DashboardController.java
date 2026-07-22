package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.application.port.in.DashboardQueryUseCase;
import com.flowpay.routing.domain.model.DashboardSnapshot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardQueryUseCase dashboardQueryUseCase;

    public DashboardController(DashboardQueryUseCase dashboardQueryUseCase) {
        this.dashboardQueryUseCase = dashboardQueryUseCase;
    }

    @GetMapping
    public DashboardSnapshot getDashboardSnapshot() {
        return dashboardQueryUseCase.getDashboardSnapshot();
    }
}
