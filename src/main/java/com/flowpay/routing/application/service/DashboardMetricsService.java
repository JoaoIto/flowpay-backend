package com.flowpay.routing.application.service;

import com.flowpay.routing.application.dto.query.DashboardSnapshot;
import com.flowpay.routing.application.dto.query.GlobalMetrics;
import com.flowpay.routing.application.dto.query.TeamMetricsResult;
import com.flowpay.routing.application.port.in.DashboardQueryUseCase;
import com.flowpay.routing.application.port.out.AgentRepositoryPort;
import com.flowpay.routing.application.port.out.ChatSessionRepositoryPort;
import com.flowpay.routing.application.port.out.QueueRepositoryPort;
import com.flowpay.routing.application.port.out.TeamRepositoryPort;
import com.flowpay.routing.domain.model.AgentStatus;
import com.flowpay.routing.domain.model.ChatStatus;
import com.flowpay.routing.domain.model.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DashboardMetricsService implements DashboardQueryUseCase {

    private final AgentRepositoryPort agentRepository;
    private final ChatSessionRepositoryPort chatRepository;
    private final QueueRepositoryPort queueRepository;
    private final TeamRepositoryPort teamRepository;

    public DashboardMetricsService(AgentRepositoryPort agentRepository,
                                   ChatSessionRepositoryPort chatRepository,
                                   QueueRepositoryPort queueRepository,
                                   TeamRepositoryPort teamRepository) {
        this.agentRepository = agentRepository;
        this.chatRepository = chatRepository;
        this.queueRepository = queueRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSnapshot getDashboardSnapshot() {
        List<Team> allTeams = teamRepository.findAll();
        List<TeamMetricsResult> teamMetricsList = allTeams.stream()
                .map(this::computeTeamMetrics)
                .collect(Collectors.toList());

        int totalActiveChats = teamMetricsList.stream().mapToInt(TeamMetricsResult::activeChats).sum();
        int totalQueuedChats = teamMetricsList.stream().mapToInt(TeamMetricsResult::queuedChats).sum();
        
        int totalAvailableAgents = agentRepository.findAllByStatus(AgentStatus.AVAILABLE).size();
        int totalLoggedInAgents = totalAvailableAgents + agentRepository.findAllByStatus(AgentStatus.ON_BREAK).size();
        
        double occupancyRatePercent = totalAvailableAgents > 0 
                ? (totalActiveChats / (3.0 * totalAvailableAgents)) * 100.0 
                : 0.0;
                
        double averageWaitTimeSeconds = teamMetricsList.stream()
                .mapToDouble(TeamMetricsResult::averageWaitTimeSeconds)
                .average()
                .orElse(0.0);
                
        double slaCompliancePercent = 100.0; 
        double abandonRatePercent = 0.0;
        
        GlobalMetrics globalMetrics = new GlobalMetrics(
                totalActiveChats,
                totalQueuedChats,
                totalAvailableAgents,
                totalLoggedInAgents,
                occupancyRatePercent,
                averageWaitTimeSeconds,
                slaCompliancePercent,
                abandonRatePercent
        );

        return new DashboardSnapshot(Instant.now(), globalMetrics, teamMetricsList);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamMetricsResult getTeamMetrics(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
        return computeTeamMetrics(team);
    }

    private TeamMetricsResult computeTeamMetrics(Team team) {
        int activeChats = (int) chatRepository.countByTeamIdAndStatus(team.getId(), ChatStatus.ACTIVE);
        int queuedChats = (int) queueRepository.countByTeamId(team.getId());
        int availableAgents = (int) agentRepository.countByTeamIdAndStatus(team.getId(), AgentStatus.AVAILABLE);
        int totalAgents = agentRepository.findByTeamId(team.getId()).size();
        
        double occupancyRate = availableAgents > 0 
                ? (activeChats / (3.0 * availableAgents)) * 100.0 
                : 0.0;
                
        double avgWaitTime = chatRepository.calculateAverageWaitTimeSeconds(team.getId());
        
        return new TeamMetricsResult(
                team.getId(), 
                team.getName(), 
                activeChats, 
                queuedChats, 
                availableAgents, 
                totalAgents, 
                occupancyRate, 
                avgWaitTime
        );
    }
}
