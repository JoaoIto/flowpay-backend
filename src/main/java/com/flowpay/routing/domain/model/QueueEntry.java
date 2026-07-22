package com.flowpay.routing.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class QueueEntry {
    private final UUID id;
    private final UUID chatId;
    private final UUID teamId;
    private final Instant enteredAt;

    public QueueEntry(UUID id, UUID chatId, UUID teamId, Instant enteredAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.chatId = Objects.requireNonNull(chatId, "chatId cannot be null");
        this.teamId = Objects.requireNonNull(teamId, "teamId cannot be null");
        this.enteredAt = Objects.requireNonNull(enteredAt, "enteredAt cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public UUID getChatId() {
        return chatId;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public Instant getEnteredAt() {
        return enteredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueEntry that = (QueueEntry) o;
        return id.equals(that.id) &&
               chatId.equals(that.chatId) &&
               teamId.equals(that.teamId) &&
               enteredAt.equals(that.enteredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, teamId, enteredAt);
    }

    @Override
    public String toString() {
        return "QueueEntry{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", teamId=" + teamId +
                ", enteredAt=" + enteredAt +
                '}';
    }
}
