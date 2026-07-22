package com.flowpay.routing.application.port.out;

import com.flowpay.routing.domain.model.QueueEntry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepositoryPort {
    QueueEntry save(QueueEntry entry);
    Optional<QueueEntry> findOldestByTeamIdForUpdate(UUID teamId);
    void remove(UUID queueEntryId);
    void removeByChatId(UUID chatId);
    long countByTeamId(UUID teamId);
    List<QueueEntry> findByTeamId(UUID teamId);
}
