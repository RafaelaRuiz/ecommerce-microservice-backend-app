package com.selimhorri.app.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Health Indicator para monitorear estado del inventario
 */
@Component("stockHealth")
@RequiredArgsConstructor
@Slf4j
public class StockHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            // Contar productos con bajo stock (< 10 unidades)
            Long lowStockCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE quantity < 10", Long.class);
            
            // Contar productos sin stock
            Long outOfStockCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE quantity = 0", Long.class);
            
            log.debug("✅ Stock health check: {} low stock, {} out of stock", 
                lowStockCount, outOfStockCount);

            Health.Builder builder = Health.up()
                .withDetail("lowStockCount", lowStockCount)
                .withDetail("outOfStockCount", outOfStockCount);

            // Warning si hay muchos productos sin stock
            if (outOfStockCount != null && outOfStockCount > 5) {
                builder.status("WARNING");
                builder.withDetail("warning", "High number of out-of-stock products");
            }

            return builder.build();
            
        } catch (Exception e) {
            log.error("❌ Stock health check: DOWN", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
