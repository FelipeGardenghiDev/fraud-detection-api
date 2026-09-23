package com.felipedev.frauddetection.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * Value Object de Domínio: Cpf (DDD).
 * Encapsula validação rigorosa de algoritmo Módulo 11 da Receita Federal,
 * formatação padrão e mascaramento para conformidade com a LGPD.
 */
@Getter
@EqualsAndHashCode
public final class Cpf {

    private static final Pattern ONLY_DIGITS = Pattern.compile("\\D");
    private final String cleanValue;

    private Cpf(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("CPF não pode ser nulo");
        }
        String digits = ONLY_DIGITS.matcher(rawValue).replaceAll("");
        if (digits.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter exatamente 11 dígitos numéricos");
        }
        if (!isValidCpf(digits)) {
            throw new IllegalArgumentException("Dígitos verificadores do CPF inválidos");
        }
        this.cleanValue = digits;
    }

    public static Cpf of(String rawValue) {
        return new Cpf(rawValue);
    }

    public static boolean isValid(String rawValue) {
        if (rawValue == null) return false;
        String digits = ONLY_DIGITS.matcher(rawValue).replaceAll("");
        if (digits.length() != 11) return false;
        return isValidCpf(digits);
    }

    private static boolean isValidCpf(String cpf) {
        // Rejeita sequências conhecidas de dígitos idênticos
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        // Validação do 1º dígito verificador
        int sum1 = 0;
        for (int i = 0; i < 9; i++) {
            sum1 += (cpf.charAt(i) - '0') * (10 - i);
        }
        int mod1 = 11 - (sum1 % 11);
        int d1 = (mod1 >= 10) ? 0 : mod1;
        if (d1 != (cpf.charAt(9) - '0')) {
            return false;
        }

        // Validação do 2º dígito verificador
        int sum2 = 0;
        for (int i = 0; i < 10; i++) {
            sum2 += (cpf.charAt(i) - '0') * (11 - i);
        }
        int mod2 = 11 - (sum2 % 11);
        int d2 = (mod2 >= 10) ? 0 : mod2;
        return d2 == (cpf.charAt(10) - '0');
    }

    public String getFormatted() {
        return String.format("%s.%s.%s-%s",
                cleanValue.substring(0, 3),
                cleanValue.substring(3, 6),
                cleanValue.substring(6, 9),
                cleanValue.substring(9, 11));
    }

    /**
     * Retorna o CPF anonimizado para conformidade com a LGPD e logs corporativos.
     * Exemplo: "***.456.789-**"
     */
    public String getMasked() {
        return String.format("***.%s.%s-**",
                cleanValue.substring(3, 6),
                cleanValue.substring(6, 9));
    }

    @Override
    public String toString() {
        return getMasked();
    }
}
