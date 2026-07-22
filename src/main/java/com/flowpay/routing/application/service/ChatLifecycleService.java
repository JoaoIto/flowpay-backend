package com.flowpay.routing.application.service;

import com.flowpay.routing.application.port.in.CloseChatUseCase;
import com.flowpay.routing.application.port.in.RouteChatUseCase;
import com.flowpay.routing.application.port.out.AgentRepositoryPort;
import com.flowpay.routing.application.port.out.ChatSessionRepositoryPort;
import com.flowpay.routing.application.port.out.EventPublisherPort;
import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.ChatSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ChatLifecycleService implements CloseChatUseCase {

    private final ChatSessionRepositoryPort chatRepository;
    private final AgentRepositoryPort agentRepository;
    private final RouteChatUseCase routingEngine;
    private final EventPublisherPort eventPublisher;

    public ChatLifecycleService(ChatSessionRepositoryPort chatRepository,
                                AgentRepositoryPort agentRepository,
                                RouteChatUseCase routingEngine,
                                EventPublisherPort eventPublisher) {
        this.chatRepository = chatRepository;
        this.agentRepository = agentRepository;
        this.routingEngine = routingEngine;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ChatSession resolveChat(UUID chatId) {
        ChatSession chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chatId));
        
        chat.resolve(Instant.now());
        chatRepository.save(chat);
        eventPublisher.publishChatResolved(chat);
        
        return chat;
    }

    @Override
    @Transactional
    public ChatSession closeChat(UUID chatId) {
        ChatSession chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chatId));
                
        chat.close(Instant.now());
        
        if (chat.getAgentId() != null) {
            Agent agent = agentRepository.findById(chat.getAgentId())
                    .orElseThrow(() -> new IllegalStateException("Agent not found: " + chat.getAgentId()));
            agent.releaseChat();
            agentRepository.save(agent);
        }
        
        chatRepository.save(chat);
        eventPublisher.publishChatClosed(chat);
        
        routingEngine.dispatchPendingChats(chat.getTeamId().toString());
        
        return chat;
    }
}
