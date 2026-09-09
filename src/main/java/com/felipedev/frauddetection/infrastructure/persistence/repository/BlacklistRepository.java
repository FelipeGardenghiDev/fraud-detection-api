package com.felipedev.frauddetection.infrastructure.persistence.repository;

import com.felipedev.frauddetection.domain.model.BlacklistType;
import com.felipedev.frauddetection.infrastructure.persistence.entity.BlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistRepository extends JpaRepository<BlacklistEntity, UUID> {

    boolean existsByTypeAndValueAndActiveTrue(BlacklistType type, String value);

    Optional<BlacklistEntity> findByTypeAndValueAndActiveTrue(BlacklistType type, String value);

    List<BlacklistEntity> findAllByActiveTrueOrderByCreatedAtDesc();
}