package com.flowpay.routing.adapter.in.web.dto;

import java.util.UUID;

public record CreateAgentRequest(String name, UUID teamId) {}
