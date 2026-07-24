package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.application.port.in.CloseChatUseCase;
import com.flowpay.routing.application.port.in.RouteChatUseCase;
import com.flowpay.routing.application.dto.command.RouteChatCommand;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chats")
@Tag(name = "Sessões de Chat", description = "Endpoints para simular roteamento e finalizar atendimentos de chat.")
public class ChatController {

    private final CloseChatUseCase closeChatUseCase;
    private final RouteChatUseCase routeChatUseCase;

    public ChatController(CloseChatUseCase closeChatUseCase, RouteChatUseCase routeChatUseCase) {
        this.closeChatUseCase = closeChatUseCase;
        this.routeChatUseCase = routeChatUseCase;
    }

    @PostMapping
    @Operation(summary = "Simular Novo Chat", description = "Força a entrada de um novo chat no sistema para testes, ignorando o Webhook do WhatsApp.")
    public ResponseEntity<Void> simulateChat(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Comando de roteamento de chat",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"customerId\": \"5511999999999\", \"teamType\": \"CARTOES\", \"source\": \"SIMULATOR\", \"subject\": \"Problema com a fatura do cartão de crédito\"}"
                    )
                )
            )
            @RequestBody RouteChatCommand command) {
        routeChatUseCase.routeChat(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolver Chat", description = "Marca o chat como resolvido com sucesso.")
    public void resolveChat(@PathVariable UUID id) {
        closeChatUseCase.resolveChat(id);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Fechar Chat", description = "Fecha o chat imediatamente, liberando o agente para novos atendimentos.")
    public void closeChat(@PathVariable UUID id) {
        closeChatUseCase.closeChat(id);
    }
}
