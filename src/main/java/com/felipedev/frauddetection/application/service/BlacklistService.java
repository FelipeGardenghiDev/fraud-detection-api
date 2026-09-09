package com.felipedev.frauddetection.application.service;

import com.felipedev.frauddetection.application.dto.BlacklistRequestDto;
import com.felipedev.frauddetection.application.dto.BlacklistResponseDto;
import com.felipedev.frauddetection.domain.model.BlacklistType;
import com.felipedev.frauddetection.infrastructure.exception.BusinessException;
import com.felipedev.frauddetection.infrastructure.exception.ResourceNotFoundException;
import com.felipedev.frauddetection.infrastructure.persistence.entity.BlacklistEntity;
import com.felipedev.frauddetection.infrastructure.persistence.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistRepository repository;

    @Transactional
    public BlacklistResponseDto addToBlacklist(BlacklistRequestDto request) {
        if (repository.existsByTypeAndValueAndActiveTrue(request.getType(), request.getValue())) {
            throw new BusinessException("Este identificador já se encontra ativo na Blacklist.");
        }

        BlacklistEntity entity = BlacklistEntity.builder()
                .type(request.getType())
                .value(request.getValue())
                .reason(request.getReason())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        BlacklistEntity saved = repository.save(entity);
        log.info("Item adicionado à Blacklist: {} (Tipo: {})", saved.getValue(), saved.getType());

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<BlacklistResponseDto> listActive() {
        return repository.findAllByActiveTrueOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deactivate(UUID id) {
        BlacklistEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de Blacklist não encontrado: " + id));

        entity.setActive(false);
        repository.save(entity);
        log.info("Registro da Blacklist desativado: {}", id);
    }

    private BlacklistResponseDto toDto(BlacklistEntity entity) {
        return BlacklistResponseDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .value(entity.getValue())
                .reason(entity.getReason())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}