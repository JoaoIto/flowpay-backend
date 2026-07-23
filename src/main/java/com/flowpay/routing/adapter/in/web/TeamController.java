package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.adapter.in.web.dto.CreateTeamRequest;
import com.flowpay.routing.application.port.in.ManageTeamUseCase;
import com.flowpay.routing.application.dto.command.CreateTeamCommand;
import com.flowpay.routing.application.dto.command.UpdateTeamCommand;
import com.flowpay.routing.domain.model.Team;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final ManageTeamUseCase manageTeamUseCase;

    public TeamController(ManageTeamUseCase manageTeamUseCase) {
        this.manageTeamUseCase = manageTeamUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@RequestBody CreateTeamRequest request) {
        CreateTeamCommand command = new CreateTeamCommand(request.name(), com.flowpay.routing.domain.model.TeamType.valueOf(request.type().toUpperCase()));
        return manageTeamUseCase.createTeam(command);
    }

    @PutMapping("/{id}")
    public Team updateTeam(@PathVariable java.util.UUID id, @RequestBody CreateTeamRequest request) {
        UpdateTeamCommand command = new UpdateTeamCommand(id, request.name());
        return manageTeamUseCase.updateTeam(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@PathVariable java.util.UUID id) {
        manageTeamUseCase.deleteTeam(id);
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return manageTeamUseCase.findAllTeams();
    }
}
