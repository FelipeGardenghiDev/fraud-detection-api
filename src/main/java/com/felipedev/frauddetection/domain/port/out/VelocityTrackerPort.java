package com.felipedev.frauddetection.domain.port.out;

/**
 * Porta de saída (Outbound Port) para rastreamento de velocidade transacional.
 * Pode ser atendida por Redis (ZSET com script Lua) ou por banco relacional.
 */
public interface VelocityTrackerPort {

    /**
     * Obtém a quantidade de transações recentes do cliente dentro da janela temporal.
     *
     * @param customerId identificador do cliente
     * @param windowMinutes tamanho da janela em minutos
     * @return contagem de transações registradas
     */
    int getRecentTransactionCount(String customerId, int windowMinutes);

    /**
     * Registra atomicamente uma nova transação na janela de velocidade do cliente.
     *
     * @param customerId identificador do cliente
     * @param transactionId identificador da transação
     * @param windowMinutes tamanho da janela em minutos
     */
    void recordTransaction(String customerId, String transactionId, int windowMinutes);
}
