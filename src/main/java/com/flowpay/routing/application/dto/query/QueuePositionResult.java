package com.flowpay.routing.application.dto.query;

import java.time.Instant;
import java.util.UUID;

public record QueuePositionResult(
    UUID chatId,
    UUID teamId,
    int position,
    Instant enteredAt
) {}
