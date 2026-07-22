package com.flowpay.routing.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "agents")
public class AgentEntity {
    @Id
    private UUID id;
    private UUID teamId;
    private String name;
    private Integer maxChats;
    private Integer activeChatsCount;
    private String status;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getTeamId() { return teamId; }
    public void setTeamId(UUID teamId) { this.teamId = teamId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getMaxChats() { return maxChats; }
    public void setMaxChats(Integer maxChats) { this.maxChats = maxChats; }
    
    public Integer getActiveChatsCount() { return activeChatsCount; }
    public void setActiveChatsCount(Integer activeChatsCount) { this.activeChatsCount = activeChatsCount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
