package com.flowpay.routing.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Team {
    private final UUID id;
    private final String name;
    private final TeamType type;
    private final Instant createdAt;

    public Team(UUID id, String name, TeamType type, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TeamType getType() {
        return type;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return id.equals(team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", createdAt=" + createdAt +
                '}';
    }
}
