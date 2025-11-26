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

        // Política de reintentos con excepciones clasificadas
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(org.springframework.web.client.ResourceAccessException.class, true);
        retryableExceptions.put(java.net.ConnectException.class, true);
        retryableExceptions.put(java.net.SocketTimeoutException.class, true);
        retryableExceptions.put(java.io.IOException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);

        // Backoff exponencial: 1s, 2s, 4s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 segundo
        backOffPolicy.setMultiplier(2.0); // Duplicar en cada intento
        backOffPolicy.setMaxInterval(10000); // Máximo 10 segundos

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Listener para logging
        retryTemplate.registerListener(new CustomRetryListener());

        return retryTemplate;
    }
    
    /**
     * Listener personalizado para logging de reintentos
     */
    private static class CustomRetryListener implements RetryListener {
        
        @Override
        public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
            // Se ejecuta antes del primer intento
            log.debug("🔄 Starting retry operation");
            return true;
        }
        
        @Override
        public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            // Se ejecuta después del último intento
            if (throwable != null) {
                log.error("❌ Retry operation exhausted after {} attempts", context.getRetryCount());
            } else {
                log.debug("✅ Retry operation completed successfully");
            }
        }
        
        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            log.warn("🔄 Retry attempt {} failed - Error: {}", 
                context.getRetryCount(), 
                throwable.getMessage());
        }
    }
}
