package com.flowpay.routing.adapter.in.web.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para alterar o status do Agente")
public record UpdateAgentStatusRequest(
    @Schema(description = "Novo status (ex: ONLINE, OFFLINE, PAUSED)", example = "ONLINE") String status
) {}
