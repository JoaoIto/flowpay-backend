package com.flowpay.routing.adapter.out.persistence;

import com.flowpay.routing.adapter.out.persistence.mapper.PersistenceMapper;
import com.flowpay.routing.adapter.out.persistence.repository.SpringAgentRepository;
import com.flowpay.routing.application.port.out.AgentRepositoryPort;
import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.AgentStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AgentPersistenceAdapter implements AgentRepositoryPort {
    private final SpringAgentRepository repository;
    private final PersistenceMapper mapper;

    public AgentPersistenceAdapter(SpringAgentRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Agent save(Agent agent) {
        return mapper.toDomain(repository.save(mapper.toEntity(agent)));
    }

    @Override
    public Optional<Agent> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Agent> findByTeamId(UUID teamId) {
        return repository.findByTeamId(teamId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Agent> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Agent> findAllByStatus(AgentStatus status) {
        return repository.findByStatus(status.name()).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public long countByTeamIdAndStatus(UUID teamId, AgentStatus status) {
        return repository.countByTeamIdAndStatus(teamId, status.name());
    }

    @Override
    public Optional<Agent> findAvailableAgentWithLock(UUID teamId) {
        return repository.findAvailableAgentWithLock(teamId).map(mapper::toDomain);
    }
}
