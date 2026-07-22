package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.application.port.in.CloseChatUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final CloseChatUseCase closeChatUseCase;

    public ChatController(CloseChatUseCase closeChatUseCase) {
        this.closeChatUseCase = closeChatUseCase;
    }

    @PostMapping("/{id}/resolve")
    public void resolveChat(@PathVariable UUID id) {
        closeChatUseCase.resolveChat(id);
    }

    @PostMapping("/{id}/close")
    public void closeChat(@PathVariable UUID id) {
        closeChatUseCase.closeChat(id);
    }
}
