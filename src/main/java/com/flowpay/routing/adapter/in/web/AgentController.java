package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.adapter.in.web.dto.CreateAgentRequest;
import com.flowpay.routing.adapter.in.web.dto.UpdateAgentStatusRequest;
import com.flowpay.routing.application.port.in.ManageAgentUseCase;
import com.flowpay.routing.application.dto.command.CreateAgentCommand;
import com.flowpay.routing.application.dto.command.UpdateAgentStatusCommand;
import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.AgentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
@Tag(name = "Gestão de Agentes", description = "Endpoints para criar, editar, listar e deletar agentes.")
public class AgentController {

    private final ManageAgentUseCase manageAgentUseCase;

    public AgentController(ManageAgentUseCase manageAgentUseCase) {
        this.manageAgentUseCase = manageAgentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar Agente", description = "Cria um novo agente vinculando-o a um Time específico.")
    public Agent createAgent(@RequestBody CreateAgentRequest request) {
        CreateAgentCommand command = new CreateAgentCommand(request.name(), request.teamId());
        return manageAgentUseCase.createAgent(command);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar Status", description = "Altera o status do agente (ex: ONLINE, OFFLINE, PAUSED). Se o status for alterado para ONLINE, ele tentará pegar chats da fila imediatamente.")
    public void updateAgentStatus(@PathVariable UUID id, @RequestBody UpdateAgentStatusRequest request) {
        UpdateAgentStatusCommand command = new UpdateAgentStatusCommand(id, AgentStatus.valueOf(request.status().toUpperCase()));
        manageAgentUseCase.updateAgentStatus(command);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Agente", description = "Atualiza o nome e a capacidade de chats simultâneos do agente.")
    public Agent updateAgent(@PathVariable UUID id, @RequestBody com.flowpay.routing.adapter.in.web.dto.UpdateAgentRequest request) {
        int maxChats = request.maxChats() != null ? request.maxChats() : 5;
        com.flowpay.routing.application.dto.command.UpdateAgentCommand command = new com.flowpay.routing.application.dto.command.UpdateAgentCommand(id, request.name(), maxChats);
        return manageAgentUseCase.updateAgent(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar Agente", description = "Remove um agente do sistema.")
    public void deleteAgent(@PathVariable UUID id) {
        manageAgentUseCase.deleteAgent(id);
    }

    @GetMapping
    @Operation(summary = "Listar Agentes", description = "Retorna todos os agentes cadastrados no sistema.")
    public List<Agent> getAllAgents() {
        return manageAgentUseCase.findAllAgents();
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Listar Agentes por Time", description = "Filtra e retorna agentes de um time específico.")
    public List<Agent> getAgentsByTeam(@PathVariable UUID teamId) {
        return manageAgentUseCase.findAgentsByTeamId(teamId);
    }
}
