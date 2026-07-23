package com.flowpay.routing.application.service;

import com.flowpay.routing.application.dto.command.CreateTeamCommand;
import com.flowpay.routing.application.port.in.ManageTeamUseCase;
import com.flowpay.routing.application.port.out.TeamRepositoryPort;
import com.flowpay.routing.domain.model.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeamManagementService implements ManageTeamUseCase {

    private final TeamRepositoryPort teamRepository;

    public TeamManagementService(TeamRepositoryPort teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    @Transactional
    public Team createTeam(CreateTeamCommand command) {
        if (teamRepository.existsByType(command.type())) {
            throw new IllegalStateException("Team of type " + command.type() + " already exists");
        }
        
        Team team = new Team(UUID.randomUUID(), command.name(), command.type(), Instant.now());
        teamRepository.save(team);
        return team;
    }

    @Override
    @Transactional
    public Team updateTeam(com.flowpay.routing.application.dto.command.UpdateTeamCommand command) {
        Team team = teamRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        Team updated = new Team(team.getId(), command.name(), team.getType(), team.getCreatedAt());
        teamRepository.save(updated);
        return updated;
    }

    @Override
    @Transactional
    public void deleteTeam(UUID teamId) {
        if (teamRepository.findById(teamId).isEmpty()) {
            throw new IllegalArgumentException("Team not found");
        }
        teamRepository.delete(teamId);
    }

    @Override
    public Optional<Team> findTeamById(UUID teamId) {
        return teamRepository.findById(teamId);
    }

    @Override
    public List<Team> findAllTeams() {
        return teamRepository.findAll();
    }
}
