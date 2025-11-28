package com.selimhorri.app.health;

import com.netflix.discovery.EurekaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Health Indicator para verificar conexión con Eureka
 */
@Component("eurekaHealth")
@RequiredArgsConstructor
@Slf4j
public class EurekaHealthIndicator implements HealthIndicator {

    private final EurekaClient eurekaClient;

    @Override
    public Health health() {
        try {
            // Verificar que el servicio esté registrado
            String serviceId = eurekaClient.getApplicationInfoManager()
                .getInfo()
                .getAppName();
            
            // Obtener lista de servicios registradossi
            List<String> registeredServices = eurekaClient.getApplications()
                .getRegisteredApplications()
                .stream()
                .map(app -> app.getName())
                .collect(Collectors.toList());

            log.debug("✅ Eureka health check: UP - Registered as: {}", serviceId);
            
            return Health.up()
                .withDetail("serviceId", serviceId)
                .withDetail("registeredServices", registeredServices)
                .withDetail("instanceCount", registeredServices.size())
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
