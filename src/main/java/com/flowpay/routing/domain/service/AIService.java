package com.flowpay.routing.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    public AIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public record AIClassification(String team, String sentiment, boolean humanRequested) {}

    public AIClassification classifyMessage(String message) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY is not set. Falling back to default routing.");
            return null; // Fallback to manual routing
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            
            String prompt = """
                    You are a highly intelligent routing assistant for a financial company.
                    Classify the following user message into exactly ONE of these teams: CARTOES, EMPRESTIMOS, OUTROS_ASSUNTOS.
                    If the user explicitly asks for a human, attendant, or real person, set human_requested to true and team to OUTROS_ASSUNTOS.
                    If the user is angry, frustrated, or uses urgent words, set sentiment to ANGRY. Otherwise set it to NEUTRAL.
                    If you are unsure, set team to OUTROS_ASSUNTOS.
                    
                    Respond strictly in valid JSON format:
                    {"team": "TEAM_NAME", "sentiment": "ANGRY|NEUTRAL", "human_requested": true|false}
                    
                    Message: "%s"
                    """.formatted(message);

            Map<String, Object> requestBody = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    },
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json"
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(url, entity, String.class);

            JsonNode root = objectMapper.readTree(response);
            String jsonOutput = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            
            JsonNode result = objectMapper.readTree(jsonOutput);
            String team = result.path("team").asText("OUTROS_ASSUNTOS");
            String sentiment = result.path("sentiment").asText("NEUTRAL");
            boolean humanRequested = result.path("human_requested").asBoolean(false);

            log.info("AI Classification result: team={}, sentiment={}, humanRequested={}", team, sentiment, humanRequested);
            return new AIClassification(team, sentiment, humanRequested);

        } catch (Exception e) {
            log.error("AI Classification failed: {}", e.getMessage());
            return null;
        }
    }

    public String suggestResponse(String chatContext) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Sugestão da IA indisponível (Chave de API não configurada).";
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;
            
            String prompt = """
                    You are a helpful and polite financial customer service agent.
                    Based on the following customer message, draft a short, professional, and empathetic response.
                    Respond in Portuguese. Keep it under 2 sentences.
                    
                    Customer Message: "%s"
                    """.formatted(chatContext);

            Map<String, Object> requestBody = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(url, entity, String.class);

            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (Exception e) {
            log.error("AI Suggestion failed: {}", e.getMessage());
            return "Erro ao gerar sugestão.";
        }
    }
}
