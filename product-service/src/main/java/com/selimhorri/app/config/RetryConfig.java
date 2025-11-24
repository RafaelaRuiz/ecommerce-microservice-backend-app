package com.selimhorri.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * PATRÓN DE RESILIENCIA: Retry Pattern
 * 
 * Propósito en Product Service:
 * - Reintentar operaciones de base de datos que fallan temporalmente
 * - Manejar deadlocks y timeouts de DB
 * - Operaciones de stock son críticas y deben ser resilientes
 */
@Configuration
@EnableRetry
@Slf4j
public class RetryConfig {
    // Spring Retry habilitado mediante @EnableRetry
}
