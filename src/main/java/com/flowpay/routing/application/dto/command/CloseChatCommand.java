package com.flowpay.routing.application.dto.command;

import java.util.UUID;

public record CloseChatCommand(UUID chatId) {
    public CloseChatCommand {
        if (chatId == null) {
            throw new IllegalArgumentException("chatId must not be null");
        }
    }
}
