package com.flowpay.routing.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowpay.routing.application.dto.query.DashboardSnapshot;
import com.flowpay.routing.application.port.out.EventPublisherPort;
import com.flowpay.routing.domain.model.Agent;
import com.flowpay.routing.domain.model.AgentStatus;
import com.flowpay.routing.domain.model.ChatSession;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RedisEventPublisherAdapter implements EventPublisherPort {

    private static final String TOPIC = "flowpay:pubsub:dashboard";

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    public RedisEventPublisherAdapter(RedissonClient redissonClient, ObjectMapper objectMapper) {
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishChatAssigned(ChatSession chat, Agent agent) {
        publish("CHAT_ASSIGNED", Map.of("chat", chat, "agent", agent));
    }

    @Override
    public void publishChatQueued(ChatSession chat, int queuePosition) {
        publish("CHAT_QUEUED", Map.of("chat", chat, "position", queuePosition));
    }

    @Override
    public void publishChatResolved(ChatSession chat) {
        publish("CHAT_RESOLVED", Map.of("chat", chat));
    }

    @Override
    public void publishChatClosed(ChatSession chat) {
        publish("CHAT_CLOSED", Map.of("chat", chat));
    }

    @Override
    public void publishAgentStatusChanged(Agent agent, AgentStatus previousStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put("agent", agent);
        if (previousStatus != null) {
            data.put("previousStatus", previousStatus);
        }
        publish("AGENT_STATUS_CHANGED", data);
    }

    @Override
    public void publishMetricsRefresh(DashboardSnapshot snapshot) {
        publish("METRICS_REFRESH", Map.of("snapshot", snapshot));
    }

    private void publish(String eventType, Object data) {
        try {
            Map<String, Object> payload = Map.of(
                "eventType", eventType,
                "data", data
            );
            String json = objectMapper.writeValueAsString(payload);
            redissonClient.getTopic(TOPIC).publish(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event: " + eventType, e);
        }
    }
}
