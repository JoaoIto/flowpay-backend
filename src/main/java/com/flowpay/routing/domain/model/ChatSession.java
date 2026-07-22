package com.flowpay.routing.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ChatSession {
    private final UUID id;
    private final UUID teamId;
    private UUID agentId;
    private final String customerId;
    private final String channel;
    private final String subject;
    private ChatStatus status;
    private final Instant createdAt;
    private Instant queuedAt;
    private Instant startedAt;
    private Instant resolvedAt;
    private Instant closedAt;

    public ChatSession(UUID id, UUID teamId, String customerId, String channel, String subject, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.teamId = Objects.requireNonNull(teamId, "teamId cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId cannot be null");
        this.channel = Objects.requireNonNull(channel, "channel cannot be null");
        this.subject = subject;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.status = ChatStatus.CREATED;
    }

    public ChatSession(UUID id, UUID teamId, UUID agentId, String customerId, String channel, String subject, ChatStatus status, Instant createdAt, Instant queuedAt, Instant startedAt, Instant resolvedAt, Instant closedAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.teamId = Objects.requireNonNull(teamId, "teamId cannot be null");
        this.agentId = agentId;
        this.customerId = Objects.requireNonNull(customerId, "customerId cannot be null");
        this.channel = Objects.requireNonNull(channel, "channel cannot be null");
        this.subject = subject;
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
        this.queuedAt = queuedAt;
        this.startedAt = startedAt;
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
    }

    public void enqueue(Instant now) {
        this.status = this.status.transitionTo(ChatStatus.QUEUED);
        this.queuedAt = Objects.requireNonNull(now, "now cannot be null");
    }

    public void activate(UUID agentId, Instant now) {
        this.agentId = Objects.requireNonNull(agentId, "agentId cannot be null");
        this.status = this.status.transitionTo(ChatStatus.ACTIVE);
        this.startedAt = Objects.requireNonNull(now, "now cannot be null");
    }

    public void resolve(Instant now) {
        this.status = this.status.transitionTo(ChatStatus.RESOLVED);
        this.resolvedAt = Objects.requireNonNull(now, "now cannot be null");
    }

    public void close(Instant now) {
        this.status = this.status.transitionTo(ChatStatus.CLOSED);
        this.closedAt = Objects.requireNonNull(now, "now cannot be null");
    }

    public boolean isInQueue() {
        return status == ChatStatus.QUEUED;
    }

    public boolean isActive() {
        return status == ChatStatus.ACTIVE;
    }

    public boolean isClosed() {
        return status == ChatStatus.CLOSED;
    }

    public Optional<Duration> getWaitDuration() {
        if (queuedAt != null && startedAt != null) {
            return Optional.of(Duration.between(queuedAt, startedAt));
        }
        return Optional.empty();
    }

    public Optional<Duration> getHandleDuration() {
        if (startedAt != null && resolvedAt != null) {
            return Optional.of(Duration.between(startedAt, resolvedAt));
        }
        return Optional.empty();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public UUID getAgentId() {
        return agentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getChannel() {
        return channel;
    }

    public String getSubject() {
        return subject;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getQueuedAt() {
        return queuedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatSession that = (ChatSession) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ChatSession{" +
                "id=" + id +
                ", teamId=" + teamId +
                ", agentId=" + agentId +
                ", customerId='" + customerId + '\'' +
                ", channel='" + channel + '\'' +
                ", subject='" + subject + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", queuedAt=" + queuedAt +
                ", startedAt=" + startedAt +
                ", resolvedAt=" + resolvedAt +
                ", closedAt=" + closedAt +
                '}';
    }
}
