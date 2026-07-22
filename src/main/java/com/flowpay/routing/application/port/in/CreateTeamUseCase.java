package com.flowpay.routing.application.port.in;

import com.flowpay.routing.application.dto.command.CreateTeamCommand;
import com.flowpay.routing.domain.model.Team;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreateTeamUseCase {
    Team createTeam(CreateTeamCommand command);
    Optional<Team> findTeamById(UUID teamId);
    List<Team> findAllTeams();
}
