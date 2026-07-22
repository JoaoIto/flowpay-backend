package com.flowpay.routing.domain.model;

import com.flowpay.routing.domain.exception.InvalidStateTransitionException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum ChatStatus {
    CREATED,
    QUEUED,
    ACTIVE,
    RESOLVED,
    CLOSED;

    private static final Map<ChatStatus, Set<ChatStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<ChatStatus, Set<ChatStatus>> map = new EnumMap<>(ChatStatus.class);
        map.put(CREATED, Collections.singleton(QUEUED));
        map.put(QUEUED, Collections.singleton(ACTIVE));
        map.put(ACTIVE, Collections.singleton(RESOLVED));
        map.put(RESOLVED, Collections.singleton(CLOSED));
        map.put(CLOSED, Collections.emptySet());
        ALLOWED_TRANSITIONS = Collections.unmodifiableMap(map);
    }

    public ChatStatus transitionTo(ChatStatus target) {
        if (!canTransitionTo(target)) {
            throw new InvalidStateTransitionException(this, target);
        }
        return target;
    }

    public boolean canTransitionTo(ChatStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(target);
    }

    public boolean isTerminal() {
        return this == CLOSED;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
