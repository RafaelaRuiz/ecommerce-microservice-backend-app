package com.selimhorri.app.resource;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthResource {

    private final HealthEndpoint healthEndpoint;

    @GetMapping("/status")
    public ResponseEntity<HealthStatus> getHealthStatus() {
        HealthComponent health = healthEndpoint.health();
        
        HealthStatus status = new HealthStatus();
        status.setOverallStatus(health.getStatus().getCode());
        status.setTimestamp(LocalDateTime.now());
        status.setComponents(extractComponents(health));
        
        return ResponseEntity.ok(status);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> liveness() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("type", "liveness");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        HealthComponent health = healthEndpoint.health();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", health.getStatus().getCode());
        response.put("type", "readiness");

        if (health instanceof Health) {
            response.put("components", ((Health) health).getDetails());
        } else if (health instanceof CompositeHealth) {
            Map<String, String> comps = new HashMap<>();
            ((CompositeHealth) health).getComponents().forEach((k, v) -> comps.put(k, v.getStatus().getCode()));
            response.put("components", comps);
        } else {
            response.put("components", null);
        }
        
        if ("UP".equals(health.getStatus().getCode())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    private Map<String, String> extractComponents(HealthComponent health) {
        Map<String, String> components = new HashMap<>();
        
        if (health instanceof CompositeHealth) {
            ((CompositeHealth) health).getComponents().forEach((key, value) -> {
                components.put(key, value.getStatus().getCode());
            });
        } else if (health instanceof Health) {
            Health h = (Health) health;
            if (h.getDetails() != null) {
                h.getDetails().forEach((key, value) -> {
                    if (value instanceof Health) {
                        components.put(key, ((Health) value).getStatus().getCode());
                    } else {
                        components.put(key, value.toString());
                    }
                });
            }
        }
        
        return components;
    }

    @Data
    private static class HealthStatus {
        private String overallStatus;
        private LocalDateTime timestamp;
        private Map<String, String> components;
    }
}
