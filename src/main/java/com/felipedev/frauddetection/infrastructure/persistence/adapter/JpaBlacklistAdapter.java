package com.felipedev.frauddetection.infrastructure.persistence.adapter;

import com.felipedev.frauddetection.domain.model.BlacklistType;
import com.felipedev.frauddetection.domain.port.out.BlacklistPort;
import com.felipedev.frauddetection.infrastructure.persistence.entity.BlacklistEntity;
import com.felipedev.frauddetection.infrastructure.persistence.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de persistência JPA para consulta à Blacklist (implementa {@link BlacklistPort}).
 */
@Component
@RequiredArgsConstructor
public class JpaBlacklistAdapter implements BlacklistPort {

    private final BlacklistRepository repository;

    @Override
    public boolean existsActive(BlacklistType type, String value) {
        if (value == null) return false;
        return repository.existsByTypeAndValueAndActiveTrue(type, value);
    }

    @Override
    public Optional<String> findActiveReason(BlacklistType type, String value) {
        if (value == null) return Optional.empty();
        return repository.findByTypeAndValueAndActiveTrue(type, value)
                .map(BlacklistEntity::getReason);
    }
}
