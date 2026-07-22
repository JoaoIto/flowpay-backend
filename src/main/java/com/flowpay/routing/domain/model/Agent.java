package com.flowpay.routing.domain.model;

import com.flowpay.routing.domain.exception.AgentUnavailableException;
import com.flowpay.routing.domain.exception.MaxCapacityExceededException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Agent {
    public static final int DEFAULT_MAX_CHATS = 3;

    private final UUID id;
    private final UUID teamId;
    private final String name;
    private final int maxChats;
    private int activeChatsCount;
    private AgentStatus status;
    private final Instant createdAt;

    public Agent(UUID id, UUID teamId, String name, int maxChats, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.teamId = Objects.requireNonNull(teamId, "teamId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.maxChats = maxChats;
        this.activeChatsCount = 0;
        this.status = AgentStatus.AVAILABLE;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public Agent(UUID id, UUID teamId, String name, int maxChats, int activeChatsCount, AgentStatus status, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.teamId = Objects.requireNonNull(teamId, "teamId cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.maxChats = maxChats;
        if (activeChatsCount > maxChats) {
            throw new IllegalArgumentException("activeChatsCount cannot exceed maxChats");
        }
        this.activeChatsCount = activeChatsCount;
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public void assignChat() {
        if (status != AgentStatus.AVAILABLE) {
            throw new AgentUnavailableException(id, status);
        }
        if (activeChatsCount >= maxChats) {
            throw new MaxCapacityExceededException(id, maxChats);
        }
        activeChatsCount++;
    }

    public void releaseChat() {
        if (activeChatsCount <= 0) {
            throw new IllegalStateException("Agent has no active chats to release");
        }
        activeChatsCount--;
    }

    public boolean hasCapacity() {
        return status == AgentStatus.AVAILABLE && activeChatsCount < maxChats;
    }

    public void changeStatus(AgentStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus cannot be null");
        if (newStatus == AgentStatus.LOGGED_OUT && activeChatsCount > 0) {
            throw new IllegalStateException("Cannot log out agent with active chats");
        }
        this.status = newStatus;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public int getMaxChats() {
        return maxChats;
    }

    public int getActiveChatsCount() {
        return activeChatsCount;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return id.equals(agent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", teamId=" + teamId +
                ", name='" + name + '\'' +
                ", maxChats=" + maxChats +
                ", activeChatsCount=" + activeChatsCount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
