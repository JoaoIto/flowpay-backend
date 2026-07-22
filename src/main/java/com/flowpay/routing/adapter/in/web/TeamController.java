package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.adapter.in.web.dto.CreateTeamRequest;
import com.flowpay.routing.application.port.in.CreateTeamUseCase;
import com.flowpay.routing.application.dto.command.CreateTeamCommand;
import com.flowpay.routing.domain.model.Team;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final CreateTeamUseCase createTeamUseCase;

    public TeamController(CreateTeamUseCase createTeamUseCase) {
        this.createTeamUseCase = createTeamUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@RequestBody CreateTeamRequest request) {
        CreateTeamCommand command = new CreateTeamCommand(request.name(), com.flowpay.routing.domain.model.TeamType.valueOf(request.type().toUpperCase()));
        return createTeamUseCase.createTeam(command);
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return createTeamUseCase.findAllTeams();
    }
}
