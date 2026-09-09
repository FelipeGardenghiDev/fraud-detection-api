package com.felipedev.frauddetection.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando header X-API-KEY estiver ausente")
    void shouldReturnUnauthorizedWhenApiKeyIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/metrics/overview"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 401 Unauthorized quando header X-API-KEY for inválido")
    void shouldReturnUnauthorizedWhenApiKeyIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/metrics/overview")
                        .header("X-API-KEY", "chave-invalida-123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 200 OK quando header X-API-KEY for válido")
    void shouldReturnOkWhenApiKeyIsValid() throws Exception {
        mockMvc.perform(get("/api/v1/metrics/overview")
                        .header("X-API-KEY", "fraud-secret-key-2026"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve permitir acesso público à documentação OpenAPI sem necessidade de API Key")
    void shouldAllowPublicAccessToSwaggerDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}