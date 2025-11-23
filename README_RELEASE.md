# Monitoreo y Observabilidad

Este documento describe las herramientas, implementación, uso y verificación del stack de observabilidad para los microservicios: `order-service`, `user-service`, `product-service`, `api-gateway`, `cloud-config`, `service-discovery`.

## Herramientas
- Prometheus: recolección de métricas técnicas y de negocio.
- Grafana: visualización y dashboards.
- Zipkin: trazabilidad distribuida.
- ELK Stack (Elasticsearch, Logstash, Kibana): gestión y análisis de logs.
- Spring Boot Actuator + Micrometer: endpoints de salud y exposición de métricas.

## Implementación
- Actuator y Prometheus:
  - Dependencias agregadas en POMs de servicios: `spring-boot-starter-actuator`, `micrometer-registry-prometheus`.
  - Configuración para exponer `actuator/prometheus` y habilitar endpoints:
    - `application.yml` y `application-dev.yml` de cada servicio: `management.endpoints.web.exposure.include="*"`, `management.endpoint.prometheus.enabled=true`.
- Métricas de negocio con Micrometer:
  - Contadores incrementados en servicios:
    - `orders_created`: en `OrderServiceImpl.save(...)`.
    - `users_registered`: en `UserServiceImpl.save(...)`.
    - `products_created`: en `ProductServiceImpl.save(...)`.
- Health checks y probes:
  - `management.endpoint.health.probes.enabled=true` en perfiles `dev` para readiness y liveness.
- Tracing distribuido (Zipkin):
  - `spring.zipkin.base-url` configurado en `application.yml` a `http://localhost:9411/`.
- ELK Stack (Logstash TCP + Logback JSON):
  - Dependencia `net.logstash.logback:logstash-logback-encoder` y `logback-spring.xml` en cada servicio para enviar logs JSON a `logstash:5000`.
- Composición del stack:
  - `observability/docker-compose.yml`: Prometheus, Grafana, Zipkin, Elasticsearch, Logstash, Kibana.
  - Prometheus scrape: `observability/prometheus/prometheus.yml` (jobs por servicio).
  - Alertas Prometheus: `observability/prometheus/alert.rules.yml` (ejemplos: InstanceDown, HighErrorRate).
  - Grafana provisioning: `observability/grafana/provisioning` (datasource Prometheus y dashboard base).
  - Logstash pipeline: `observability/logstash/pipeline/logstash.conf`.

## Cómo ejecutar el stack
- Requisitos: Docker y Docker Compose.
- Comandos:
```
cd observability
docker compose up -d
```
- Puertos y UIs:
  - Prometheus: `http://localhost:9090`
  - Grafana: `http://localhost:3000` (usuario `admin`, contraseña por defecto `admin` si no se cambió)
  - Zipkin: `http://localhost:9411`
  - Kibana: `http://localhost:5601`

## Endpoints de métricas y salud
- Order: `http://localhost:8300/order-service/actuator/prometheus`, `.../actuator/health`, `.../actuator/health/liveness`, `.../actuator/health/readiness`
- User: `http://localhost:8700/user-service/actuator/prometheus` y salud similar.
- Product: `http://localhost:8500/product-service/actuator/prometheus` y salud similar.
- API Gateway: `http://localhost:8080/actuator/prometheus`.
- Cloud Config: `http://localhost:9296/actuator/prometheus`.
- Service Discovery: `http://localhost:8761/actuator/prometheus`.

## Uso de Prometheus
- Verificar targets: `Status → Targets` deben estar `UP`.
- Consultas de ejemplo:
  - Error rate: `rate(http_server_requests_seconds_count{outcome="ERROR"}[5m])`
  - Negocio:
    - `sum(increase(orders_created_total[10m]))`
    - `sum(increase(users_registered_total[10m]))`
    - `sum(increase(products_created_total[10m]))`
- Alertas:
  - Regla `InstanceDown` y `HighErrorRate` en `alert.rules.yml`. Integra Alertmanager si deseas notificaciones externas.

## Uso de Grafana
- Datasource Prometheus preconfigurado: `http://prometheus:9090`.
- Dashboard base: `Microservices Overview` con paneles de error rate y contadores de negocio.
- Crear dashboards por servicio:
  - JVM Memory: `jvm_memory_used_bytes` (si está expuesto), latencia por endpoint: `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri))`.

## Uso de Zipkin
- Abrir `http://localhost:9411`.
- Generar tráfico (ej. pruebas, Locust) y visualizar trazas por `serviceName` (`USER-SERVICE`, `ORDER-SERVICE`, etc.).

## Uso de ELK
- Logstash escucha TCP en `5000` y almacena en Elasticsearch índice `microservices-logs-*`.
- Kibana:
  - Crear Index Pattern: `microservices-logs-*`.
  - Visualizar logs, filtrar por `service`/`level` y crear dashboards.

## Verificación de extremo a extremo
- Levantar servicios y el stack de observabilidad.
- Golpear endpoints (o usar `Locust`).
- Confirmar:
  - Prometheus targets `UP` y métricas incrementando.
  - Grafana mostrando paneles sin errores.
  - Zipkin con trazas recientes.
  - Kibana con logs ingresando desde los servicios.

## Notas
- En Linux, reemplazar `host.docker.internal` por la IP del host o usar red `host`.
- Para alertas con notificaciones, añade `Alertmanager` y configura receptores (Slack, Email, PagerDuty).

