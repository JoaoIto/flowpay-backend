package com.flowpay.routing.application.port.out;

import com.flowpay.routing.application.dto.query.DashboardSnapshot;
import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.AgentStatus;
import com.flowpay.routing.domain.model.ChatSession;

public interface EventPublisherPort {
    void publishChatAssigned(ChatSession chat, Agent agent);
    void publishChatQueued(ChatSession chat, int queuePosition);
    void publishChatResolved(ChatSession chat);
    void publishChatClosed(ChatSession chat);
    void publishAgentStatusChanged(Agent agent, AgentStatus previousStatus);
    void publishMetricsRefresh(DashboardSnapshot snapshot);
}
