package com.felipedev.frauddetection.infrastructure.cache;

import com.felipedev.frauddetection.infrastructure.persistence.repository.FraudAnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
public class RedisVelocityTracker {

    private final StringRedisTemplate redisTemplate;
    private final FraudAnalysisRepository analysisRepository;

    @Value("${fraud.redis.enabled:true}")
    private boolean redisEnabled;

    public RedisVelocityTracker(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            FraudAnalysisRepository analysisRepository) {
        this.redisTemplate = redisTemplate;
        this.analysisRepository = analysisRepository;
    }

    public int getRecentTransactionCount(String customerId, int windowMinutes) {
        if (!redisEnabled || redisTemplate == null) {
            return countFromDatabase(customerId, windowMinutes);
        }

        try {
            String key = buildKey(customerId);
            String val = redisTemplate.opsForValue().get(key);
            if (val != null) {
                return Integer.parseInt(val);
            }
            int countFromDb = countFromDatabase(customerId, windowMinutes);
            redisTemplate.opsForValue().set(key, String.valueOf(countFromDb), Duration.ofMinutes(windowMinutes));
            return countFromDb;
        } catch (Exception ex) {
            log.debug("Redis não acessível. Utilizando fallback resiliente para banco relacional: {}", ex.getMessage());
            return countFromDatabase(customerId, windowMinutes);
        }
    }

    public void incrementTransactionCount(String customerId, int windowMinutes) {
        if (!redisEnabled || redisTemplate == null) {
            return;
        }

        try {
            String key = buildKey(customerId);
            Long current = redisTemplate.opsForValue().increment(key);
            if (current != null && current == 1L) {
                redisTemplate.expire(key, Duration.ofMinutes(windowMinutes));
            }
            log.debug("Contador de velocidade no Redis incrementado para {}: {}", customerId, current);
        } catch (Exception ex) {
            log.debug("Falha ao incrementar contador no Redis para {}: {}", customerId, ex.getMessage());
        }
    }

    private int countFromDatabase(String customerId, int windowMinutes) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(windowMinutes);
        return (int) analysisRepository.countByCustomerIdAndAnalyzedAtAfter(customerId, windowStart);
    }

    private String buildKey(String customerId) {
        return "fraud:velocity:" + customerId;
    }
}