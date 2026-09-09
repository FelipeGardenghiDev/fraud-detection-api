package com.felipedev.frauddetection.infrastructure.controller;

import com.felipedev.frauddetection.application.dto.BlacklistRequestDto;
import com.felipedev.frauddetection.application.dto.BlacklistResponseDto;
import com.felipedev.frauddetection.application.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
@Tag(name = "Blacklist Restritiva", description = "Gestão de CPFs, IPs e Dispositivos bloqueados por fraude confirmada")
public class BlacklistController {

    private final BlacklistService service;

    @PostMapping
    @Operation(summary = "Adicionar um identificador à Blacklist")
    @ApiResponse(responseCode = "201", description = "Item incluído com sucesso na Blacklist")
    @ApiResponse(responseCode = "422", description = "Item já cadastrado e ativo")
    public ResponseEntity<BlacklistResponseDto> addToBlacklist(@Valid @RequestBody BlacklistRequestDto request) {
        BlacklistResponseDto response = service.addToBlacklist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os itens ativos na Blacklist")
    public ResponseEntity<List<BlacklistResponseDto>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar/remover um item da Blacklist")
    @ApiResponse(responseCode = "204", description = "Item desativado com sucesso")
    @ApiResponse(responseCode = "404", description = "Item não encontrado")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}