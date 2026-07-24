package com.flowpay.routing.adapter.in.web;

import com.flowpay.routing.domain.service.AIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AISuggestionController {

    private final AIService aiService;

    public AISuggestionController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/suggest")
    public ResponseEntity<Map<String, String>> suggest(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        String suggestion = aiService.suggestResponse(message);
        return ResponseEntity.ok(Map.of("suggestion", suggestion));
    }
}
