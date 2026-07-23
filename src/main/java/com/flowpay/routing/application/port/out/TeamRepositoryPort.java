package com.flowpay.routing.application.port.out;

import com.flowpay.routing.domain.model.Team;
import com.flowpay.routing.domain.model.TeamType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepositoryPort {
    Team save(Team team);
    Optional<Team> findById(UUID id);
    Optional<Team> findByType(TeamType type);
    List<Team> findAll();
    boolean existsByType(TeamType type);
    void delete(UUID id);
}
