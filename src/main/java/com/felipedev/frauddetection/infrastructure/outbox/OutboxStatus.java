package com.felipedev.frauddetection.infrastructure.outbox;

public enum OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
