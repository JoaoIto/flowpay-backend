package com.flowpay.routing.application.service;

import com.flowpay.routing.application.dto.command.RouteChatCommand;
import com.flowpay.routing.application.port.in.RouteChatUseCase;
import com.flowpay.routing.application.port.out.AgentRepositoryPort;
import com.flowpay.routing.application.port.out.ChatSessionRepositoryPort;
import com.flowpay.routing.application.port.out.DistributedLockPort;
import com.flowpay.routing.application.port.out.EventPublisherPort;
import com.flowpay.routing.application.port.out.QueueRepositoryPort;
import com.flowpay.routing.application.port.out.TeamRepositoryPort;
import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.ChatSession;
import com.flowpay.routing.domain.model.QueueEntry;
import com.flowpay.routing.domain.model.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoutingEngineService implements RouteChatUseCase {

    private final TeamRepositoryPort teamRepository;
    private final AgentRepositoryPort agentRepository;
    private final ChatSessionRepositoryPort chatRepository;
    private final QueueRepositoryPort queueRepository;
    private final DistributedLockPort distributedLock;
    private final EventPublisherPort eventPublisher;

    public RoutingEngineService(TeamRepositoryPort teamRepository,
                                AgentRepositoryPort agentRepository,
                                ChatSessionRepositoryPort chatRepository,
                                QueueRepositoryPort queueRepository,
                                DistributedLockPort distributedLock,
                                EventPublisherPort eventPublisher) {
        this.teamRepository = teamRepository;
        this.agentRepository = agentRepository;
        this.chatRepository = chatRepository;
        this.queueRepository = queueRepository;
        this.distributedLock = distributedLock;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ChatSession routeChat(RouteChatCommand command) {
        Team team = teamRepository.findByType(command.teamType())
                .orElseThrow(() -> new IllegalArgumentException("Team not found for type: " + command.teamType()));

        ChatSession chat = new ChatSession(UUID.randomUUID(), team.getId(), command.customerId(), command.channel(), command.subject(), Instant.now());
        chat.enqueue(Instant.now());
        
        chatRepository.save(chat);

        QueueEntry queueEntry = new QueueEntry(UUID.randomUUID(), chat.getId(), team.getId(), Instant.now());
        queueRepository.save(queueEntry);

        eventPublisher.publishChatQueued(chat, 1);

        dispatchPendingChats(team.getId().toString());

        return chatRepository.findById(chat.getId()).orElse(chat);
    }

    @Override
    public void dispatchPendingChats(String teamId) {
        String lockKey = "flowpay:lock:routing:team:" + teamId;
        try {
            if (distributedLock.tryAcquire(lockKey, 2000, 10000)) {
                try {
                    executeDistributionLoop(UUID.fromString(teamId));
                } finally {
                    distributedLock.release(lockKey);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while trying to acquire lock for team " + teamId, e);
        }
    }

    @Transactional
    private void executeDistributionLoop(UUID teamId) {
        while (true) {
            Optional<QueueEntry> optionalEntry = queueRepository.findOldestByTeamIdForUpdate(teamId);
            if (optionalEntry.isEmpty()) {
                break;
            }
            QueueEntry entry = optionalEntry.get();

            Optional<Agent> optionalAgent = agentRepository.findAvailableAgentWithLock(teamId);
            if (optionalAgent.isPresent()) {
                Agent agent = optionalAgent.get();
                ChatSession chat = chatRepository.findById(entry.getChatId())
                        .orElseThrow(() -> new IllegalStateException("Chat not found: " + entry.getChatId()));

                agent.assignChat();
                chat.activate(agent.getId(), Instant.now());

                agentRepository.save(agent);
                chatRepository.save(chat);
                queueRepository.remove(entry.getId());

                eventPublisher.publishChatAssigned(chat, agent);
            } else {
                break;
            }
        }
    }
}
