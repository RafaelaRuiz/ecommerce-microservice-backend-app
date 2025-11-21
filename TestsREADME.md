# Guía de Pruebas (microservicios)

Este documento describe las pruebas disponibles, cómo están implementadas y cómo ejecutarlas en los microservicios: `order-service`, `user-service`, `product-service`, `api-gateway`, `cloud-config`, `service-discovery`.

## Alcance
- Pruebas unitarias en servicios y controladores.
- Pruebas de integración web (contexto Spring + MockMvc).
- Esqueleto de pruebas E2E a través de API Gateway.
- Pruebas de rendimiento y estrés con Locust.
- Pruebas de seguridad (OWASP ZAP Baseline Scan).
- Informes de cobertura Jacoco y reporte agregado.
- Ejecución automatizada en CI (GitHub Actions).

## Tipos de pruebas
- Unitarias: validan lógica de negocio y mapeos.
- Integración web: validan rutas REST en contexto de aplicación sin dependencias externas.
- E2E (esqueleto): punto de entrada para flujos completos vía API Gateway.
- Rendimiento/estrés: carga concurrente sobre endpoints principales.
- Seguridad: escaneo pasivo OWASP ZAP contra la superficie HTTP.

## Implementación por servicio
- order-service
  - Unitarias: `OrderServiceImplTest` (servicio y mapeos).
  - Web slice: `OrderResourceTest` (`@WebMvcTest`).
  - Integración: `OrderResourceIT` (`@SpringBootTest + @AutoConfigureMockMvc`).
- user-service
  - Unitarias: `UserServiceImplTest` (incluye construcción de `Credential` y `RoleBasedAuthority`).
  - Web slice: `UserResourceTest`.
  - Integración: `UserResourceIT`.
- product-service
  - Unitarias: `ProductServiceImplTest` (incluye `Category`).
  - Web slice: `ProductResourceTest`.
  - Integración: `ProductResourceIT`.
- api-gateway
  - E2E esqueleto: `E2EFlowTest` (deshabilitado por defecto con `@Disabled`).
- cloud-config / service-discovery
  - Pruebas de arranque de contexto (existentes) y perfil de `test` para aislar servicios externos.

## Perfiles de prueba
Para cada microservicio se agregó `application-test.yml` con:
- BD H2 o deshabilitar conexiones externas.
- `spring.cloud.config.enabled=false`, `eureka.client.enabled=false` donde aplica.
- Puertos aleatorios (`server.port: 0`) para evitar colisiones.

Ubicaciones:
- `order-service/src/test/resources/application-test.yml`
- `user-service/src/test/resources/application-test.yml`
- `product-service/src/test/resources/application-test.yml`
- `api-gateway/src/test/resources/application-test.yml`
- `cloud-config/src/test/resources/application-test.yml`
- `service-discovery/src/test/resources/application-test.yml`

## Ejecución local
- Ejecutar todas las pruebas (unitarias + integración):
```
./mvnw.cmd -DskipTests=false clean verify
```
- Ejecutar pruebas de un servicio específico (ejemplo `user-service`):
```
./mvnw.cmd -pl :user-service -DskipTests=false test
```
- Generar reporte agregado de cobertura Jacoco:
```
./mvnw.cmd jacoco:report-aggregate
```
El reporte agregado queda en `target/site/jacoco-aggregate`.

## Rendimiento y estrés (Locust)
- Script:
  - `tests/performance/locustfile.py`
- Requisitos: `python3`, `pip`, `locust`.
- Instalación y ejecución headless:
```
pip install locust
TARGET_HOST=http://localhost:8080 locust -f tests/performance/locustfile.py --headless -u 50 -r 5 -t 2m
```
- Endpoints cubiertos (a través de API Gateway):
  - `/user-service/api/users`
  - `/product-service/api/products`
  - `/order-service/api/orders`

## Seguridad (OWASP ZAP)
- Escaneo baseline en CI usando `zaproxy/action-baseline`.
- Parámetro `target_url` vía `workflow_dispatch` para apuntar a un entorno desplegado.

## CI/CD (GitHub Actions)
- Workflow: `.github/workflows/ci.yml`.
- Jobs:
  - `build-test`: compila, ejecuta pruebas, sube artefactos JUnit y genera cobertura Jacoco agregada.
  - `performance`: ejecuta Locust en modo headless (opcional, vía `workflow_dispatch`).
  - `security`: ejecuta ZAP Baseline Scan (opcional, vía `workflow_dispatch`).
- Artefactos:
  - JUnit (`**/target/surefire-reports/**`, `**/target/failsafe-reports/**`).
  - Cobertura (`target/site/jacoco-aggregate`).

## Estructura principal de pruebas
- order-service
  - `src/test/java/com/selimhorri/app/service/OrderServiceImplTest.java`
  - `src/test/java/com/selimhorri/app/resource/OrderResourceTest.java`
  - `src/test/java/com/selimhorri/app/resource/OrderResourceIT.java`
- user-service
  - `src/test/java/com/selimhorri/app/service/UserServiceImplTest.java`
  - `src/test/java/com/selimhorri/app/resource/UserResourceTest.java`
  - `src/test/java/com/selimhorri/app/resource/UserResourceIT.java`
- product-service
  - `src/test/java/com/selimhorri/app/service/ProductServiceImplTest.java`
  - `src/test/java/com/selimhorri/app/resource/ProductResourceTest.java`
  - `src/test/java/com/selimhorri/app/resource/ProductResourceIT.java`
- api-gateway
  - `src/test/java/com/selimhorri/app/e2e/E2EFlowTest.java`

## Buenas prácticas
- Mantener objetos anidados requeridos por helpers de mapeo (p. ej. `Credential` en `User`, `Category` en `Product`, `Cart` en `Order`).
- Usar `@WebMvcTest` para pruebas de controladores aislados y `@SpringBootTest` + `@AutoConfigureMockMvc` para integración.
- Aislar perfiles y dependencias externas en `application-test.yml`.

## Solución de problemas
- Errores de NPE en mapeos: construir entidades/DTOs con los objetos anidados requeridos.
- Conflictos de puertos: se usa `server.port: 0` en `test`.
- Config Server/Eureka no disponible en test: se deshabilita en el perfil `test`.