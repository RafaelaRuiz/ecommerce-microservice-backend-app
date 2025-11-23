package com.selimhorri.app.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("databaseHealth")
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            if (result != null && result == 1) {
                Long orderCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM orders", Long.class);
                
                log.debug("✅ Database health check: UP ({} orders)", orderCount);
                
                return Health.up()
                    .withDetail("database", "MySQL")
                    .withDetail("status", "Connected")
                    .withDetail("orderCount", orderCount)
                    .build();
            } else {
                return Health.down().withDetail("error", "Unexpected result").build();
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
