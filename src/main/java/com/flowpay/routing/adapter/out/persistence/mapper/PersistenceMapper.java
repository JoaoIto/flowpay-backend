package com.flowpay.routing.adapter.out.persistence.mapper;

import com.flowpay.routing.adapter.out.persistence.entity.AgentEntity;
import com.flowpay.routing.adapter.out.persistence.entity.ChatSessionEntity;
import com.flowpay.routing.adapter.out.persistence.entity.QueueEntryEntity;
import com.flowpay.routing.adapter.out.persistence.entity.TeamEntity;
import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.AgentStatus;
import com.flowpay.routing.domain.model.ChatSession;
import com.flowpay.routing.domain.model.ChatStatus;
import com.flowpay.routing.domain.model.QueueEntry;
import com.flowpay.routing.domain.model.Team;
import com.flowpay.routing.domain.model.TeamType;
import org.springframework.stereotype.Component;

@Component
public class PersistenceMapper {

    public Team toDomain(TeamEntity entity) {
        if (entity == null) return null;
        return new Team(
            entity.getId(),
            entity.getName(),
            TeamType.fromString(entity.getType()).orElseThrow(),
            entity.getCreatedAt()
        );
    }

    public TeamEntity toEntity(Team domain) {
        if (domain == null) return null;
        TeamEntity entity = new TeamEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setType(domain.getType().name());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public Agent toDomain(AgentEntity entity) {
        if (entity == null) return null;
        return new Agent(
            entity.getId(),
            entity.getTeamId(),
            entity.getName(),
            entity.getMaxChats(),
            entity.getActiveChatsCount(),
            AgentStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt()
        );
    }

    public AgentEntity toEntity(Agent domain) {
        if (domain == null) return null;
        AgentEntity entity = new AgentEntity();
        entity.setId(domain.getId());
        entity.setTeamId(domain.getTeamId());
        entity.setName(domain.getName());
        entity.setMaxChats(domain.getMaxChats());
        entity.setActiveChatsCount(domain.getActiveChatsCount());
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public ChatSession toDomain(ChatSessionEntity entity) {
        if (entity == null) return null;
        return new ChatSession(
            entity.getId(),
            entity.getTeamId(),
            entity.getAgentId(),
            entity.getCustomerId(),
            entity.getChannel(),
            entity.getSubject(),
            ChatStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getQueuedAt(),
            entity.getStartedAt(),
            entity.getResolvedAt(),
            entity.getClosedAt()
        );
    }

    public ChatSessionEntity toEntity(ChatSession domain) {
        if (domain == null) return null;
        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setId(domain.getId());
        entity.setTeamId(domain.getTeamId());
        entity.setAgentId(domain.getAgentId());
        entity.setCustomerId(domain.getCustomerId());
        entity.setChannel(domain.getChannel());
        entity.setSubject(domain.getSubject());
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setQueuedAt(domain.getQueuedAt());
        entity.setStartedAt(domain.getStartedAt());
        entity.setResolvedAt(domain.getResolvedAt());
        entity.setClosedAt(domain.getClosedAt());
        return entity;
    }

    public QueueEntry toDomain(QueueEntryEntity entity) {
        if (entity == null) return null;
        return new QueueEntry(
            entity.getId(),
            entity.getChatId(),
            entity.getTeamId(),
            entity.getEnteredAt()
        );
    }

    public QueueEntryEntity toEntity(QueueEntry domain) {
        if (domain == null) return null;
        QueueEntryEntity entity = new QueueEntryEntity();
        entity.setId(domain.getId());
        entity.setChatId(domain.getChatId());
        entity.setTeamId(domain.getTeamId());
        entity.setEnteredAt(domain.getEnteredAt());
        return entity;
    }
}
