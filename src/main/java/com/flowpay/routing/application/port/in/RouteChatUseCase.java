package com.flowpay.routing.application.port.in;

import com.flowpay.routing.application.dto.command.RouteChatCommand;
import com.flowpay.routing.domain.model.ChatSession;

public interface RouteChatUseCase {
    ChatSession routeChat(RouteChatCommand command);
    void dispatchPendingChats(String teamId);
}
