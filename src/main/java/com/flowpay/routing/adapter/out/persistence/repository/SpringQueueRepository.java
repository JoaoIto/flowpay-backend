package com.flowpay.routing.adapter.out.persistence.repository;

import com.flowpay.routing.adapter.out.persistence.entity.QueueEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringQueueRepository extends JpaRepository<QueueEntryEntity, UUID> {
    void deleteByChatId(UUID chatId);
    long countByTeamId(UUID teamId);
    List<QueueEntryEntity> findByTeamIdOrderByEnteredAtAsc(UUID teamId);

    @Query(value = "SELECT * FROM queue_entries WHERE team_id = :teamId ORDER BY entered_at ASC LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<QueueEntryEntity> findOldestByTeamIdForUpdate(@Param("teamId") UUID teamId);
}
