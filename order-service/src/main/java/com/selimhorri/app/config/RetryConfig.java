package com.selimhorri.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * PATRÓN DE RESILIENCIA: Retry Pattern con Backoff Exponencial
 * 
 * Propósito:
 * - Manejar fallos transitorios en llamadas a servicios externos
 * - Reintentar automáticamente con delays crecientes
 * - Evitar sobrecarga de servicios degradados
 * 
 * Implementado en Order Service porque:
 * - Depende de Product Service y User Service
 * - Las órdenes son operaciones críticas de negocio
 * - Fallos transitorios no deben cancelar órdenes
 */
@Configuration
@EnableRetry
@Slf4j
public class RetryConfig {

    /**
     * RetryTemplate configurado para llamadas a servicios externos
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Política de reintentos
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // Máximo 3 intentos
        
        // Excepciones que disparan retry
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(org.springframework.web.client.ResourceAccessException.class, true);
        retryableExceptions.put(java.net.ConnectException.class, true);
        retryableExceptions.put(java.net.SocketTimeoutException.class, true);
        retryableExceptions.put(java.io.IOException.class, true);
        retryPolicy.setRetryableExceptions(retryableExceptions);

        // Backoff exponencial: 1s, 2s, 4s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 segundo
        backOffPolicy.setMultiplier(2.0); // Duplicar en cada intento
        backOffPolicy.setMaxInterval(10000); // Máximo 10 segundos

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Listener para logging
        retryTemplate.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(
                    RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                log.warn("🔄 Retry attempt {} for operation: {} - Error: {}",
                    context.getRetryCount(),
                    callback.getClass().getSimpleName(),
                    throwable.getMessage());
            }
        });

        return retryTemplate;
    }
}
