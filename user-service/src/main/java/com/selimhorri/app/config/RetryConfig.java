package com.selimhorri.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * PATRÓN DE RESILIENCIA: Retry Pattern
 * 
 * Propósito en User Service:
 * - Reintentar operaciones de autenticación/credenciales
 * - Manejar timeouts de base de datos
 * - Operaciones de usuario son críticas para autenticación
 */
@Configuration
@EnableRetry
@Slf4j
public class RetryConfig {
    // Spring Retry habilitado mediante @EnableRetry
}
