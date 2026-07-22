package com.flowpay.routing.domain.exception;

public class InvalidStateTransitionException extends RuntimeException {

    private final String fromStatus;
    private final String toStatus;

    public InvalidStateTransitionException(Enum<?> from, Enum<?> to) {
        super(String.format(
            "Invalid state transition: cannot move from %s to %s. Lifecycle is strictly monotonic: CREATED → QUEUED → ACTIVE → RESOLVED → CLOSED",
            from.name(), to.name()
        ));
        this.fromStatus = from.name();
        this.toStatus = to.name();
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }
}
