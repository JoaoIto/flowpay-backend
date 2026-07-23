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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final ManageAgentUseCase manageAgentUseCase;

    public AgentController(ManageAgentUseCase manageAgentUseCase) {
        this.manageAgentUseCase = manageAgentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Agent createAgent(@RequestBody CreateAgentRequest request) {
        CreateAgentCommand command = new CreateAgentCommand(request.name(), request.teamId());
        return manageAgentUseCase.createAgent(command);
    }

    @PatchMapping("/{id}/status")
    public void updateAgentStatus(@PathVariable UUID id, @RequestBody UpdateAgentStatusRequest request) {
        UpdateAgentStatusCommand command = new UpdateAgentStatusCommand(id, AgentStatus.valueOf(request.status().toUpperCase()));
        manageAgentUseCase.updateAgentStatus(command);
    }

    @PutMapping("/{id}")
    public Agent updateAgent(@PathVariable UUID id, @RequestBody com.flowpay.routing.adapter.in.web.dto.UpdateAgentRequest request) {
        int maxChats = request.maxChats() != null ? request.maxChats() : 5;
        com.flowpay.routing.application.dto.command.UpdateAgentCommand command = new com.flowpay.routing.application.dto.command.UpdateAgentCommand(id, request.name(), maxChats);
        return manageAgentUseCase.updateAgent(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAgent(@PathVariable UUID id) {
        manageAgentUseCase.deleteAgent(id);
    }

    @GetMapping
    public List<Agent> getAllAgents() {
        return manageAgentUseCase.findAllAgents();
    }

    @GetMapping("/team/{teamId}")
    public List<Agent> getAgentsByTeam(@PathVariable UUID teamId) {
        return manageAgentUseCase.findAgentsByTeamId(teamId);
    }
}
