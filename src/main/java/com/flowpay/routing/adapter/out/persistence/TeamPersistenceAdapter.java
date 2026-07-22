package com.flowpay.routing.adapter.out.persistence;

import com.flowpay.routing.adapter.out.persistence.mapper.PersistenceMapper;
import com.flowpay.routing.adapter.out.persistence.repository.SpringTeamRepository;
import com.flowpay.routing.application.port.out.TeamRepositoryPort;
import com.flowpay.routing.domain.model.Team;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TeamPersistenceAdapter implements TeamRepositoryPort {
    private final SpringTeamRepository repository;
    private final PersistenceMapper mapper;

    public TeamPersistenceAdapter(SpringTeamRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Team save(Team team) {
        return mapper.toDomain(repository.save(mapper.toEntity(team)));
    }

    @Override
    public Optional<Team> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Team> findByType(String type) {
        return repository.findByType(type).map(mapper::toDomain);
    }

    @Override
    public boolean existsByType(String type) {
        return repository.existsByType(type);
    }
}
