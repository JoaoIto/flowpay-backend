package com.flowpay.routing.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "queue_entries")
public class QueueEntryEntity {
    @Id
    private UUID id;
    private UUID chatId;
    private UUID teamId;
    private OffsetDateTime enteredAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getChatId() { return chatId; }
    public void setChatId(UUID chatId) { this.chatId = chatId; }

    public UUID getTeamId() { return teamId; }
    public void setTeamId(UUID teamId) { this.teamId = teamId; }

    public OffsetDateTime getEnteredAt() { return enteredAt; }
    public void setEnteredAt(OffsetDateTime enteredAt) { this.enteredAt = enteredAt; }
}
