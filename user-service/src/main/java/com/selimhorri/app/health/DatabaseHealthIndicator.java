package com.selimhorri.app.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Health Indicator para verificar conectividad con la base de datos
 */
@Component("databaseHealth")
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            // Verificar conexión ejecutando query simple
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            if (result != null && result == 1) {
                log.debug("✅ Database health check: UP");
                return Health.up()
                    .withDetail("database", "MySQL")
                    .withDetail("status", "Connected")
                    .withDetail("query", "SELECT 1")
                    .build();
            } else {
                log.warn("⚠️ Database health check: Unexpected result");
                return Health.down()
                    .withDetail("error", "Unexpected query result")
                    .build();
            }
        } catch (Exception e) {
            log.error("❌ Database health check: DOWN", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
