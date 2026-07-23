package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.application.port.in.CloseChatUseCase;
import com.flowpay.routing.application.port.in.RouteChatUseCase;
import com.flowpay.routing.application.dto.command.RouteChatCommand;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {

    private final CloseChatUseCase closeChatUseCase;
    private final RouteChatUseCase routeChatUseCase;

    public ChatController(CloseChatUseCase closeChatUseCase, RouteChatUseCase routeChatUseCase) {
        this.closeChatUseCase = closeChatUseCase;
        this.routeChatUseCase = routeChatUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> simulateChat(@RequestBody RouteChatCommand command) {
        routeChatUseCase.routeChat(command);
        return ResponseEntity.ok().build();
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
