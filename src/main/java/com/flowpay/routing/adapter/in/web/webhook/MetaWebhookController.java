package com.flowpay.routing.adapter.in.web.webhook;

import com.flowpay.routing.adapter.in.web.webhook.dto.MetaWebhookPayload;
import com.flowpay.routing.application.port.in.RouteChatUseCase;
import com.flowpay.routing.application.dto.command.RouteChatCommand;
import com.flowpay.routing.domain.model.TeamType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/webhooks/meta")
@Tag(name = "Webhook - WhatsApp", description = "Endpoints para recebimento de mensagens do WhatsApp via Meta Cloud API")
public class MetaWebhookController {

    private static final String VERIFY_TOKEN = "ubots_flowpay_secret_123";

    private final RouteChatUseCase routeChatUseCase;

    public MetaWebhookController(RouteChatUseCase routeChatUseCase) {
        this.routeChatUseCase = routeChatUseCase;
    }

    @GetMapping
    @Operation(summary = "Verificação de Webhook", description = "Usado pela Meta para confirmar a URL do webhook através de um challenge.")
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && VERIFY_TOKEN.equals(token)) {
            return ResponseEntity.ok(challenge);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping
    @Operation(summary = "Receber Mensagem do WhatsApp", description = "Ponto de entrada principal do motor FlowPay. Ao enviar o payload, a mensagem entrará na fila e o dashboard será atualizado via SSE instantaneamente.")
    public ResponseEntity<Void> receiveWebhook(@RequestBody MetaWebhookPayload payload) {
        if (payload.entry() != null) {
            for (MetaWebhookPayload.Entry entry : payload.entry()) {
                if (entry.changes() != null) {
                    for (MetaWebhookPayload.Change change : entry.changes()) {
                        if (change.value() != null && change.value().messages() != null) {
                            for (MetaWebhookPayload.Message message : change.value().messages()) {
                                String customerId = message.from();
                                String subject = message.text() != null ? message.text().body() : "";
                                
                                TeamType teamType;
                                String lowerSubject = subject.toLowerCase();
                                if (lowerSubject.contains("cartao") || lowerSubject.contains("cartão")) {
                                    teamType = TeamType.CARTOES;
                                } else if (lowerSubject.contains("emprestimo")) {
                                    teamType = TeamType.EMPRESTIMOS;
                                } else {
                                    teamType = TeamType.OUTROS_ASSUNTOS;
                                }

                                RouteChatCommand command = new RouteChatCommand(customerId, teamType, "WHATSAPP", subject);
                                routeChatUseCase.routeChat(command);
                            }
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok().build();
    }
}
