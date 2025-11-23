package com.selimhorri.app.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Health Indicator para monitorear estado de órdenes
 */
@Component("orderProcessingHealth")
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            // Contar órdenes por estado
            Long totalOrders = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM orders", Long.class);
            
            log.debug("✅ Order processing health: {} total orders", totalOrders);

            Health.Builder builder = Health.up()
                .withDetail("totalOrders", totalOrders);

            // Warning si no hay órdenes (posible problema)
            if (totalOrders != null && totalOrders == 0) {
                builder.status("WARNING");
                builder.withDetail("warning", "No orders in system");
            }

            return builder.build();
            
        } catch (Exception e) {
            log.error("❌ Order processing health: DOWN", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
