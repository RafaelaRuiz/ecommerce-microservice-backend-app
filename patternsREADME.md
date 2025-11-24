# Arquitectura y Patrones en los Microservicios

Este proyecto implementa una arquitectura de microservicios basada en Spring Boot, desplegada en Azure usando Terraform y Kubernetes. A continuación se describen los patrones arquitectónicos aplicados, su propósito y su implementación en los seis microservicios principales:

- **user-service**
- **product-service**
- **order-service**
- **api-gateway**
- **cloud-config**
- **service-discovery**

---

## 1. Health Check Pattern (Patrón de Salud)

**Propósito:**
Permite monitorear el estado de cada microservicio, facilitando la detección temprana de fallos y la integración con orquestadores (Kubernetes) y herramientas de monitoreo.

**Implementación:**

- Todos los microservicios exponen endpoints de salud (`/actuator/health`, `/actuator/health/readiness`, `/actuator/health/liveness`) usando Spring Boot Actuator.
- Endpoints personalizados (`/health/ready`, `/health/live`) implementados en `HealthResource.java` para mayor control y compatibilidad con Kubernetes probes.
- Ejemplo de endpoint personalizado:

```java
@RestController
@RequestMapping("/health")
public class HealthResource {
    private final HealthEndpoint healthEndpoint;
    @GetMapping("/ready")
    public ResponseEntity<?> readiness() {
        HealthComponent health = healthEndpoint.health();
        // ... lógica para readiness
    }
    @GetMapping("/live")
    public ResponseEntity<?> liveness() {
        HealthComponent health = healthEndpoint.health();
        // ... lógica para liveness
    }
}
```

**Servicios:**

- Implementado y mejorado en: user-service, product-service, order-service, api-gateway, cloud-config, service-discovery.

---

## 2. Retry Pattern (Patrón de Reintento)

**Propósito:**
Aumenta la resiliencia ante fallos transitorios en la comunicación entre microservicios o con recursos externos (bases de datos, APIs). Permite reintentar operaciones fallidas antes de propagar el error.

**Implementación:**

- Uso de Spring Retry y AOP para aplicar reintentos automáticos en métodos críticos.
- Configuración centralizada en `RetryConfig.java` con política de reintentos y backoff exponencial.
- Implementación de `CustomRetryListener` para logging y trazabilidad de reintentos.
- Ejemplo de configuración:

```java
@Configuration
@EnableRetry
public class RetryConfig {
    @Bean
    public RetryTemplate retryTemplate() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(SQLException.class, true);
        SimpleRetryPolicy policy = new SimpleRetryPolicy(3, retryableExceptions);
        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(500);
        backOff.setMultiplier(2);
        backOff.setMaxInterval(5000);
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(policy);
        template.setBackOffPolicy(backOff);
        template.registerListener(new CustomRetryListener());
        return template;
    }
}
```

**Servicios:**

- Implementado en: user-service, product-service, order-service.
- No necesario en: api-gateway, cloud-config, service-discovery (no realizan llamadas críticas a recursos externos).

---

## 3. Configuration Pattern (Patrón de Configuración Centralizada y Segura)

**Propósito:**
Permite gestionar la configuración de los microservicios de forma centralizada, segura y desacoplada del código fuente. Facilita la gestión de secretos y la adaptación a diferentes entornos.

**Implementación:**

- **Spring Cloud Config**: Centraliza la configuración en el microservicio cloud-config, que expone la configuración a los demás servicios.
- **Variables de entorno**: Todas las credenciales y parámetros sensibles se inyectan como variables de entorno, nunca en el código fuente.
- **Terraform + Azure Key Vault**: Los secretos se gestionan en Azure Key Vault y se referencian desde Terraform, que los inyecta en los pods de Kubernetes como Secrets.
- **Kubernetes ConfigMaps y Secrets**: Configuración no sensible en ConfigMaps, secretos en Secrets.
- Ejemplo de referencia en `application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
```

**Servicios:**

- Implementado en todos los microservicios.

---

## 4. Service Discovery Pattern (Patrón de Descubrimiento de Servicios)

**Propósito:**
Permite que los microservicios encuentren y se comuniquen entre sí dinámicamente, sin necesidad de conocer direcciones fijas.

**Implementación:**

- Uso de **Eureka** (service-discovery) como servidor de descubrimiento.
- Los microservicios se registran automáticamente en Eureka y resuelven las direcciones de otros servicios a través de él.

**Servicios:**

- Implementado en todos los microservicios.

---

## 5. API Gateway Pattern (Patrón de Puerta de Enlace)

**Propósito:**
Centraliza el acceso a los microservicios, gestionando rutas, autenticación, balanceo de carga y políticas transversales.

**Implementación:**

- Uso de **Spring Cloud Gateway** en el microservicio api-gateway.
- Configuración de rutas y filtros en `application.yml` y/o Java.

**Servicios:**

- Implementado en: api-gateway.

---

## 6. Infraestructura como Código (IaC)

**Propósito:**
Permite definir y gestionar toda la infraestructura (AKS, Key Vault, PostgreSQL, etc.) de forma declarativa y reproducible.

**Implementación:**

- Uso de **Terraform** en la carpeta `infra/terraform`.
- Definición de recursos de Azure: AKS, Key Vault, PostgreSQL, ConfigMaps, Secrets, etc.
- Ejemplo de recurso Key Vault en Terraform:

```hcl
resource "azurerm_key_vault" "main" {
  name                = var.key_vault_name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  ...
}
```

---

## 7. Observabilidad y Monitoreo

**Propósito:**
Permite recolectar métricas, trazas y logs para monitorear el estado y desempeño de los microservicios.

**Implementación:**

- **Spring Boot Actuator**: expone métricas y endpoints de salud.
- **Zipkin**: para trazabilidad distribuida (ver carpeta infra/k8s/zipkin).
- Integración con Azure Monitor y Application Insights (opcional, vía configuración).

---

## Resumen de Patrones por Microservicio

| Microservicio     | Health Check | Retry | Config Centralizada | Service Discovery | API Gateway | Observabilidad |
| ----------------- | ------------ | ----- | ------------------- | ----------------- | ----------- | -------------- |
| user-service      | ✔️           | ✔️    | ✔️                  | ✔️                |             | ✔️             |
| product-service   | ✔️           | ✔️    | ✔️                  | ✔️                |             | ✔️             |
| order-service     | ✔️           | ✔️    | ✔️                  | ✔️                |             | ✔️             |
| api-gateway       | ✔️           |       | ✔️                  | ✔️                | ✔️          | ✔️             |
| cloud-config      | ✔️           |       | ✔️                  | ✔️                |             | ✔️             |
| service-discovery | ✔️           |       | ✔️                  | ✔️                |             | ✔️             |

---

## Referencias

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Spring Retry](https://github.com/spring-projects/spring-retry)
- [Spring Cloud Config](https://cloud.spring.io/spring-cloud-config/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Eureka](https://cloud.spring.io/spring-cloud-netflix/multi/multi_spring-cloud-eureka-server.html)
- [Terraform Azure Provider](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)
- [Azure Key Vault](https://learn.microsoft.com/en-us/azure/key-vault/)

---

Este README resume los patrones arquitectónicos implementados y mejorados en el proyecto, su propósito y su aplicación concreta en cada microservicio. Para más detalles, consulta la documentación y los archivos fuente correspondientes.
