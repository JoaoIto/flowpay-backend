package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.adapter.in.web.dto.CreateTeamRequest;
import com.flowpay.routing.application.port.in.ManageTeamUseCase;
import com.flowpay.routing.application.dto.command.CreateTeamCommand;
import com.flowpay.routing.application.dto.command.UpdateTeamCommand;
import com.flowpay.routing.domain.model.Team;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "Gestão de Times (Equipes)", description = "Endpoints para administrar as equipes de atendimento.")
public class TeamController {

    private final ManageTeamUseCase manageTeamUseCase;

    public TeamController(ManageTeamUseCase manageTeamUseCase) {
        this.manageTeamUseCase = manageTeamUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar Time", description = "Cria um novo time/fila de atendimento (ex: Suporte, Vendas).")
    public Team createTeam(@RequestBody CreateTeamRequest request) {
        CreateTeamCommand command = new CreateTeamCommand(request.name(), com.flowpay.routing.domain.model.TeamType.valueOf(request.type().toUpperCase()));
        return manageTeamUseCase.createTeam(command);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Time", description = "Atualiza o nome de um time.")
    public Team updateTeam(@PathVariable java.util.UUID id, @RequestBody CreateTeamRequest request) {
        UpdateTeamCommand command = new UpdateTeamCommand(id, request.name());
        return manageTeamUseCase.updateTeam(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar Time", description = "Deleta um time do sistema.")
    public void deleteTeam(@PathVariable java.util.UUID id) {
        manageTeamUseCase.deleteTeam(id);
    }

    @GetMapping
    @Operation(summary = "Listar Times", description = "Retorna todos os times.")
    public List<Team> getAllTeams() {
        return manageTeamUseCase.findAllTeams();
    }
}
