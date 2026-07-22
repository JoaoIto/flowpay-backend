package com.flowpay.routing.adapter.out.persistence.repository;

import com.flowpay.routing.adapter.out.persistence.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SpringTeamRepository extends JpaRepository<TeamEntity, UUID> {
    Optional<TeamEntity> findByType(String type);
    boolean existsByType(String type);
}
