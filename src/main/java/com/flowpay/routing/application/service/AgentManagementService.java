package com.flowpay.routing.application.service;

import com.flowpay.routing.application.dto.command.CreateAgentCommand;
import com.flowpay.routing.application.dto.command.UpdateAgentStatusCommand;
import com.flowpay.routing.application.port.in.ManageAgentUseCase;
import com.flowpay.routing.application.port.in.RouteChatUseCase;
import com.flowpay.routing.application.port.out.AgentRepositoryPort;
import com.flowpay.routing.application.port.out.EventPublisherPort;
import com.flowpay.routing.application.port.out.TeamRepositoryPort;
import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.AgentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AgentManagementService implements ManageAgentUseCase {

    private final AgentRepositoryPort agentRepository;
    private final TeamRepositoryPort teamRepository;
    private final RouteChatUseCase routingEngine;
    private final EventPublisherPort eventPublisher;

    public AgentManagementService(AgentRepositoryPort agentRepository,
                                  TeamRepositoryPort teamRepository,
                                  RouteChatUseCase routingEngine,
                                  EventPublisherPort eventPublisher) {
        this.agentRepository = agentRepository;
        this.teamRepository = teamRepository;
        this.routingEngine = routingEngine;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Agent createAgent(CreateAgentCommand command) {
        teamRepository.findById(command.teamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + command.teamId()));

        Agent agent = new Agent(UUID.randomUUID(), command.teamId(), command.name(), Agent.DEFAULT_MAX_CHATS, Instant.now());
        agentRepository.save(agent);
        return agent;
    }

    @Override
    @Transactional
    public Agent updateAgentStatus(UpdateAgentStatusCommand command) {
        Agent agent = agentRepository.findById(command.agentId())
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + command.agentId()));

        AgentStatus previousStatus = agent.getStatus();
        agent.changeStatus(command.newStatus());
        
        agentRepository.save(agent);
        eventPublisher.publishAgentStatusChanged(agent, previousStatus);
        
        if (command.newStatus() == AgentStatus.AVAILABLE && previousStatus != AgentStatus.AVAILABLE) {
            routingEngine.dispatchPendingChats(agent.getTeamId().toString());
        }
        
        return agent;
    }

    @Override
    public Optional<Agent> findAgentById(UUID agentId) {
        return agentRepository.findById(agentId);
    }

    @Override
    public List<Agent> findAgentsByTeamId(UUID teamId) {
        return agentRepository.findByTeamId(teamId);
    }

    @Override
    public List<Agent> findAllAgents() {
        return agentRepository.findAll();
    }
}
