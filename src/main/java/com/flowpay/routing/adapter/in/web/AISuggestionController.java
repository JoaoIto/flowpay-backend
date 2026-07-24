package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.domain.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "Inteligência Artificial (Gemini)", description = "Endpoints para gerar respostas inteligentes baseadas no contexto financeiro.")
public class AISuggestionController {

    private final AIService aiService;

    public AISuggestionController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/suggest")
    @Operation(summary = "Gerar Sugestão de Resposta", description = "Usa o Google Gemini para analisar a mensagem do cliente e sugerir uma resposta ideal para o agente.")
    public ResponseEntity<Map<String, String>> suggest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Mensagem enviada pelo cliente",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"message\": \"Quero cancelar meu cartão porque as taxas estão altas.\"}"
                    )
                )
            )
            @RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        String suggestion = aiService.suggestResponse(message);
        return ResponseEntity.ok(Map.of("suggestion", suggestion));
    }
}
