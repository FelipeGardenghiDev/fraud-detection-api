package com.felipedev.frauddetection.domain.model;

public enum Decision {
    APPROVED("Transação aprovada com baixo risco."),
    SUSPICIOUS("Transação suspeita. Encaminhada para análise manual."),
    BLOCKED("Transação bloqueada devido a alto risco de fraude.");

    private final String description;

    Decision(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static Decision fromScore(int score) {
        if (score >= 80) {
            return BLOCKED;
        } else if (score >= 40) {
            return SUSPICIOUS;
        } else {
            return APPROVED;
        }
    }
}