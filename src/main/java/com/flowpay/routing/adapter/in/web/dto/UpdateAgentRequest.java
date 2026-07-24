package com.flowpay.routing.adapter.in.web.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para atualização de configurações do Agente")
public record UpdateAgentRequest(
    @Schema(description = "Novo nome do agente", example = "João Victor PFR") String name, 
    @Schema(description = "Limite máximo de chats simultâneos", example = "4") Integer maxChats
) {}
