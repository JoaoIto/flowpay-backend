package com.flowpay.routing.adapter.out.persistence;

import com.flowpay.routing.adapter.out.persistence.mapper.PersistenceMapper;
import com.flowpay.routing.adapter.out.persistence.repository.SpringQueueRepository;
import com.flowpay.routing.application.port.out.QueueRepositoryPort;
import com.flowpay.routing.domain.model.QueueEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class QueuePersistenceAdapter implements QueueRepositoryPort {
    private final SpringQueueRepository repository;
    private final PersistenceMapper mapper;

    public QueuePersistenceAdapter(SpringQueueRepository repository, PersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public QueueEntry save(QueueEntry queueEntry) {
        return mapper.toDomain(repository.save(mapper.toEntity(queueEntry)));
    }

    public Optional<QueueEntry> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void removeByChatId(UUID chatId) {
        repository.deleteByChatId(chatId);
    }

    @Override
    public void remove(UUID queueEntryId) {
        repository.deleteById(queueEntryId);
    }

    @Override
    public long countByTeamId(UUID teamId) {
        return repository.countByTeamId(teamId);
    }

    @Override
    public List<QueueEntry> findByTeamId(UUID teamId) {
        return repository.findByTeamIdOrderByEnteredAtAsc(teamId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    public List<QueueEntry> findByTeamIdOrderByEnteredAtAsc(UUID teamId) {
        return repository.findByTeamIdOrderByEnteredAtAsc(teamId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<QueueEntry> findOldestByTeamIdForUpdate(UUID teamId) {
        return repository.findOldestByTeamIdForUpdate(teamId).map(mapper::toDomain);
    }
}
