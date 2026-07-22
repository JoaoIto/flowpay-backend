package com.flowpay.routing.application.port.in;

import com.flowpay.routing.domain.model.ChatSession;
import java.util.UUID;

public interface CloseChatUseCase {
    ChatSession resolveChat(UUID chatId);
    ChatSession closeChat(UUID chatId);
}
