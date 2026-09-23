package com.felipedev.frauddetection.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfTest {

    // CPFs válidos conhecidos gerados por algoritmo oficial
    private static final String VALID_CPF_1 = "52998224725";
    private static final String VALID_CPF_FORMATTED = "529.982.247-25";

    @Test
    @DisplayName("Deve criar Value Object Cpf para valor válido")
    void shouldCreateCpfForValidValue() {
        Cpf cpf = Cpf.of(VALID_CPF_1);

        assertNotNull(cpf);
        assertEquals(VALID_CPF_1, cpf.getCleanValue());
        assertEquals(VALID_CPF_FORMATTED, cpf.getFormatted());
        assertEquals("***.982.247-**", cpf.getMasked());
    }

    @Test
    @DisplayName("Deve aceitar CPF formatado e extrair dígitos limpos")
    void shouldAcceptFormattedCpf() {
        Cpf cpf = Cpf.of(VALID_CPF_FORMATTED);

        assertEquals(VALID_CPF_1, cpf.getCleanValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11111111111",
            "00000000000",
            "99999999999",
            "12345678900", // Dígito verificador inválido
            "123",         // Tamanho insuficiente
            "1234567890123"// Tamanho excessivo
    })
    @DisplayName("Deve rejeitar CPFs inválidos ou com sequências repetidas")
    void shouldRejectInvalidCpf(String invalidCpf) {
        assertThrows(IllegalArgumentException.class, () -> Cpf.of(invalidCpf));
        assertFalse(Cpf.isValid(invalidCpf));
    }

    @Test
    @DisplayName("Deve validar nulo como inválido")
    void shouldRejectNullCpf() {
        assertThrows(IllegalArgumentException.class, () -> Cpf.of(null));
        assertFalse(Cpf.isValid(null));
    }
}
