package com.flowpay.routing.adapter.in.web.dto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criação de uma nova Equipe (Time)")
public record CreateTeamRequest(
    @Schema(description = "Nome do time", example = "Suporte Nível 1") String name, 
    @Schema(description = "Tipo de atendimento", example = "SUPPORT") String type
) {}
