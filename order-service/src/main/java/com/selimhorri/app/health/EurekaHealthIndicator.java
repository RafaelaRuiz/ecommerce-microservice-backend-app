package com.selimhorri.app.health;

import com.netflix.discovery.EurekaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("eurekaHealth")
@RequiredArgsConstructor
@Slf4j
public class EurekaHealthIndicator implements HealthIndicator {

    private final EurekaClient eurekaClient;

    @Override
    public Health health() {
        try {
            String serviceId = eurekaClient.getApplicationInfoManager()
                .getInfo()
                .getAppName();
            
            log.debug("✅ Eureka health check: UP - Registered as: {}", serviceId);
            
            return Health.up()
                .withDetail("serviceId", serviceId)
                .withDetail("status", "Registered")
                .build();
                
        } catch (Exception e) {
            log.error("❌ Eureka health check: DOWN", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withException(e)
                .build();
        }
    }
}
