package com.flowpay.routing.adapter.in.web.dto;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação de um novo Agente")
public record CreateAgentRequest(
    @Schema(description = "Nome do agente", example = "João Victor") String name, 
    @Schema(description = "ID do time ao qual o agente pertence", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID teamId
) {}
