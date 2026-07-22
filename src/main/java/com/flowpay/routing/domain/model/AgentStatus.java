package com.flowpay.routing.domain.model;

public enum AgentStatus {
    AVAILABLE,
    ON_BREAK,
    LOGGED_OUT;

    public boolean isOnline() {
        return this == AVAILABLE || this == ON_BREAK;
    }
}
