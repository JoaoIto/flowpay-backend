package com.flowpay.routing.domain.exception;

import java.util.UUID;

public class MaxCapacityExceededException extends RuntimeException {

    private final UUID agentId;
    private final int maxCapacity;

    public MaxCapacityExceededException(UUID agentId, int maxCapacity) {
        super(String.format(
            "Agent %s has reached the maximum capacity of %d simultaneous chats",
            agentId, maxCapacity
        ));
        this.agentId = agentId;
        this.maxCapacity = maxCapacity;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}
