package com.felipedev.frauddetection.infrastructure.cache;

import com.felipedev.frauddetection.domain.port.out.VelocityTrackerPort;
import com.felipedev.frauddetection.infrastructure.persistence.repository.FraudAnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * Rastreador de velocidade transacional com Redis Sliding Window via Script Lua Atômico.
 * Implementa {@link VelocityTrackerPort} com fallback resiliente para banco relacional.
 */
@Slf4j
@Service
public class RedisVelocityTracker implements VelocityTrackerPort {

    private final StringRedisTemplate redisTemplate;
    private final FraudAnalysisRepository analysisRepository;

    @Value("${fraud.redis.enabled:true}")
    private boolean redisEnabled;

    /**
     * Script Lua para janela deslizante atômica com Redis ZSET.
     * 1. Remove timestamps fora da janela (now - windowMillis)
     * 2. Se txId informado, adiciona a transação atual com score = now
     * 3. Retorna a contagem exata no intervalo
     * 4. Atualiza o TTL da chave
     */
    private static final String ATOMIC_SLIDING_WINDOW_LUA = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local windowMillis = tonumber(ARGV[2])
            local txId = ARGV[3]
            local threshold = now - windowMillis
            
            redis.call('ZREMRANGEBYSCORE', key, '-inf', threshold)
            if txId and txId ~= '' then
                redis.call('ZADD', key, now, txId)
            end
            local count = redis.call('ZCARD', key)
            local ttl = math.ceil(windowMillis / 1000)
            redis.call('EXPIRE', key, math.max(ttl, 60))
            return count
            """;

    private final DefaultRedisScript<Long> slidingWindowScript;

    public RedisVelocityTracker(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            FraudAnalysisRepository analysisRepository) {
        this.redisTemplate = redisTemplate;
        this.analysisRepository = analysisRepository;
        this.slidingWindowScript = new DefaultRedisScript<>(ATOMIC_SLIDING_WINDOW_LUA, Long.class);
    }

    @Override
    public int getRecentTransactionCount(String customerId, int windowMinutes) {
        if (!redisEnabled || redisTemplate == null) {
            return countFromDatabase(customerId, windowMinutes);
        }

        try {
            String key = buildZsetKey(customerId);
            long now = Instant.now().toEpochMilli();
            long windowMillis = (long) windowMinutes * 60 * 1000;

            Long count = redisTemplate.execute(
                    slidingWindowScript,
                    Collections.singletonList(key),
                    String.valueOf(now),
                    String.valueOf(windowMillis),
                    ""
            );

            if (count != null) {
                return count.intValue();
            }

            return countFromDatabase(customerId, windowMinutes);
        } catch (Exception ex) {
            log.debug("Redis não acessível. Utilizando fallback resiliente para banco relacional: {}", ex.getMessage());
            return countFromDatabase(customerId, windowMinutes);
        }
    }

    @Override
    public void recordTransaction(String customerId, String transactionId, int windowMinutes) {
        if (!redisEnabled || redisTemplate == null) {
            return;
        }

        try {
            String key = buildZsetKey(customerId);
            long now = Instant.now().toEpochMilli();
            long windowMillis = (long) windowMinutes * 60 * 1000;
            String txId = transactionId != null ? transactionId : UUID.randomUUID().toString();

            redisTemplate.execute(
                    slidingWindowScript,
                    Collections.singletonList(key),
                    String.valueOf(now),
                    String.valueOf(windowMillis),
                    txId
            );
            log.debug("Transação {} registrada no Sliding Window Redis para cliente {}", txId, customerId);
        } catch (Exception ex) {
            log.debug("Falha ao registrar transação no Redis para {}: {}", customerId, ex.getMessage());
        }
    }

    /**
     * Método retrocompatível com implementações anteriores.
     */
    public void incrementTransactionCount(String customerId, int windowMinutes) {
        recordTransaction(customerId, UUID.randomUUID().toString(), windowMinutes);
    }

    private int countFromDatabase(String customerId, int windowMinutes) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(windowMinutes);
        return (int) analysisRepository.countByCustomerIdAndAnalyzedAtAfter(customerId, windowStart);
    }

    private String buildZsetKey(String customerId) {
        return "fraud:velocity:zset:" + customerId;
    }
}