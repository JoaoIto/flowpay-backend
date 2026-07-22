package com.flowpay.routing.adapter.out.persistence;

import com.flowpay.routing.adapter.out.persistence.mapper.PersistenceMapper;
import com.flowpay.routing.adapter.out.persistence.repository.SpringChatSessionRepository;
import com.flowpay.routing.application.port.out.ChatSessionRepositoryPort;
import com.flowpay.routing.domain.model.ChatSession;
import com.flowpay.routing.domain.model.ChatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ChatSessionPersistenceAdapter implements ChatSessionRepositoryPort {
    private final SpringChatSessionRepository repository;
    private final PersistenceMapper mapper;

    public ChatSessionPersistenceAdapter(SpringChatSessionRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ChatSession save(ChatSession chatSession) {
        return mapper.toDomain(repository.save(mapper.toEntity(chatSession)));
    }

    @Override
    public Optional<ChatSession> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ChatSession> findByTeamIdAndStatus(UUID teamId, ChatStatus status) {
        return repository.findByTeamIdAndStatus(teamId, status.name()).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ChatSession> findByAgentIdAndStatus(UUID agentId, ChatStatus status) {
        return repository.findByAgentIdAndStatus(agentId, status.name()).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ChatSession> findByStatus(ChatStatus status) {
        return repository.findByStatus(status.name()).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByStatus(ChatStatus status) {
        return repository.countByStatus(status.name());
    }

    @Override
    public long countByTeamIdAndStatus(UUID teamId, ChatStatus status) {
        return repository.countByTeamIdAndStatus(teamId, status.name());
    }

    @Override
    public double calculateAverageWaitTimeSeconds(UUID teamId) {
        return repository.calculateAverageWaitTimeSeconds(teamId);
    }

    @Override
    public long countChatsWithWaitTimeUnder(UUID teamId, int maxWaitSeconds) {
        return repository.countChatsWithWaitTimeUnder(teamId, maxWaitSeconds);
    }

    @Override
    public long countAbandonedChats(UUID teamId) {
        return repository.countAbandonedChats(teamId);
    }
}
