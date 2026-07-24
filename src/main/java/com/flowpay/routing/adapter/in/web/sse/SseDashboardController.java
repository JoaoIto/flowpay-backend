package com.flowpay.routing.adapter.in.web.sse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/dashboard/stream")
public class SseDashboardController {

    private final RedisMessageSubscriber redisMessageSubscriber;
    private final com.flowpay.routing.application.port.in.DashboardQueryUseCase dashboardQueryUseCase;

    public SseDashboardController(RedisMessageSubscriber redisMessageSubscriber,
                                  com.flowpay.routing.application.port.in.DashboardQueryUseCase dashboardQueryUseCase) {
        this.redisMessageSubscriber = redisMessageSubscriber;
        this.dashboardQueryUseCase = dashboardQueryUseCase;
    }

    @GetMapping
    public SseEmitter streamDashboard() {
        SseEmitter emitter = new SseEmitter(3600000L); // 1 hour timeout
        redisMessageSubscriber.addEmitter(emitter);
        
        // Send initial payload immediately
        try {
            emitter.send(dashboardQueryUseCase.getDashboardSnapshot());
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
}
