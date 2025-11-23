# Documentación Completa - Taller 2: Ecommerce Microservices Backend

---

## 📋 Tabla de Contenidos

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Arquitectura del Sistema](#2-arquitectura-del-sistema)
3. [Componentes Implementados](#3-componentes-implementados)
4. [Configuración de CI/CD](#4-configuración-de-cicd)
5. [Proceso de Despliegue](#5-proceso-de-despliegue)
6. [Pruebas y Validación](#6-pruebas-y-validación)
7. [Monitoreo y Trazabilidad](#7-monitoreo-y-trazabilidad)
8. [Conclusiones y Resultados](#8-conclusiones-y-resultados)
9. [Anexos](#9-anexos)

---

## 1. Resumen Ejecutivo

### Objetivo del Proyecto

Implementar una arquitectura de microservicios para una aplicación de e-commerce utilizando tecnologías modernas de contenedorización, orquestación y CI/CD, cumpliendo con los principios de **Cloud Native** y **12-Factor App**.

### Tecnologías Utilizadas

| Categoría                | Tecnología           | Versión      | Propósito                    |
| ------------------------ | -------------------- | ------------ | ---------------------------- |
| **Lenguaje**             | Java                 | 11 (Temurin) | Backend de microservicios    |
| **Framework**            | Spring Boot          | 2.x          | Desarrollo de microservicios |
| **Contenedorización**    | Docker               | Latest       | Empaquetado de aplicaciones  |
| **Orquestación (Local)** | Docker Compose       | 3.x          | Despliegue local             |
| **Orquestación (Cloud)** | Kubernetes           | 1.20+        | Despliegue en producción     |
| **CI/CD**                | GitHub Actions       | N/A          | Automatización de pipelines  |
| **Registro de Imágenes** | Docker Hub           | N/A          | Almacenamiento de imágenes   |
| **Service Discovery**    | Eureka Server        | Netflix OSS  | Registro de servicios        |
| **API Gateway**          | Spring Cloud Gateway | 2.x          | Enrutamiento centralizado    |
| **Distributed Tracing**  | Zipkin               | Latest       | Trazabilidad de peticiones   |
| **Configuration Server** | Spring Cloud Config  | 2.x          | Configuración centralizada   |
| **IaC**                  | Terraform            | 1.6+         | Infraestructura como código  |
| **Cloud Provider**       | Azure AKS            | N/A          | Kubernetes en la nube        |

### Resultados Alcanzados

✅ **7 microservicios** funcionando correctamente  
✅ **18 workflows CI/CD** automatizados (6 servicios × 3 ambientes)  
✅ **Imágenes Docker** publicadas en Docker Hub con estrategia de tags  
✅ **Service Discovery** operativo con Eureka  
✅ **Distributed Tracing** implementado con Zipkin  
✅ **Health checks** configurados en todos los servicios  
✅ **Infraestructura como Código** lista con Terraform para Azure

---

## 2. Arquitectura del Sistema

### 2.1 Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                     CAPA DE USUARIO                              │
│                   (Clientes HTTP/REST)                           │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│                     API GATEWAY (8080)                           │
│  - Spring Cloud Gateway                                          │
│  - Circuit Breaker (Resilience4j)                               │
│  - Load Balancing                                               │
└────────────────────────┬────────────────────────────────────────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ↓              ↓              ↓
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│USER-SERVICE │  │ORDER-SERVICE│  │PRODUCT-     │
│   (8700)    │  │   (8300)    │  │SERVICE(8500)│
└─────────────┘  └─────────────┘  └─────────────┘
          │              │              │
          └──────────────┼──────────────┘
                         │
          ┌──────────────┴──────────────┐
          │                             │
          ↓                             ↓
┌─────────────────────┐     ┌─────────────────────┐
│  EUREKA SERVER      │     │  ZIPKIN             │
│  (8761)             │     │  (9411)             │
│  - Service Registry │     │  - Distributed      │
│  - Health Checks    │     │    Tracing          │
└─────────────────────┘     └─────────────────────┘
          │
          ↓
┌─────────────────────┐
│  CLOUD CONFIG       │
│  (9296)             │
│  - Centralized      │
│    Configuration    │
└─────────────────────┘
```

### 2.2 Flujo de Comunicación

1. **Cliente** → API Gateway (puerto 8080)
2. **API Gateway** → Service Discovery (Eureka) para resolver servicios
3. **API Gateway** → Microservicio específico (balanceo de carga)
4. **Microservicio** → Envía trazas a Zipkin
5. **Microservicio** → Registra su estado en Eureka

### 2.3 Estrategia de Tags Docker

| Environment     | Branch Git | Tag Docker | Propósito                              |
| --------------- | ---------- | ---------- | -------------------------------------- |
| **Development** | `dev`      | `:dev`     | Desarrollo activo, cambios frecuentes  |
| **Staging**     | `stage`    | `:stage`   | Pre-producción, pruebas de integración |
| **Production**  | `master`   | `:latest`  | Producción estable                     |

**Ejemplos de imágenes:**

- `rafaelaruiz/user-service-ecommerce-boot:dev`
- `rafaelaruiz/user-service-ecommerce-boot:stage`
- `rafaelaruiz/user-service-ecommerce-boot:latest`

---

## 3. Componentes Implementados

### 3.1 Microservicios Core

#### **Service Discovery (Eureka Server)**

**Ubicación:** `service-discovery/`

**Puerto:** 8761

**Propósito:** Registro y descubrimiento dinámico de servicios.

**Dockerfile:** `service-discovery/Dockerfile`

```dockerfile
FROM eclipse-temurin:11-jre-jammy
ARG JAR_FILE=target/*.jar
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /home/app

COPY ${JAR_FILE} app.jar
RUN chown app:app /home/app/app.jar

USER app

EXPOSE 8761
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s \
	CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar /home/app/app.jar"]
```

---

#### **Cloud Config Server**

**Ubicación:** `cloud-config/`

**Puerto:** 9296

**Propósito:** Configuración centralizada para todos los microservicios.

**Configuración:** `cloud-config/src/main/resources/application.yml`

```yaml
server:
  port: 9296

spring:
  application:
    name: CLOUD-CONFIG
  cloud:
    config:
      server:
        git:
          uri: https://github.com/SelimHorri/cloud-config-server
          clone-on-start: true

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://localhost:8761/eureka/}
    register-with-eureka: true
    fetch-registry: true
```

---

#### **API Gateway**

**Ubicación:** `api-gateway/`

**Puerto:** 8080

**Propósito:** Punto de entrada único para todos los microservicios, con enrutamiento, balanceo de carga y circuit breaker.

**Circuit Breaker (Resilience4j):**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      apiGateway:
        register-health-indicator: true
        failure-rate-threshold: 50
        minimum-number-of-calls: 5
        sliding-window-size: 10
```

---

#### **User Service**

**Ubicación:** `user-service/`

**Puerto:** 8700

**Propósito:** Gestión de usuarios, autenticación y autorización.

---

#### **Order Service**

**Ubicación:** `order-service/`

**Puerto:** 8300

**Propósito:** Gestión de pedidos basados en carritos de compra.

---

#### **Product Service**

**Ubicación:** `product-service/`

**Puerto:** 8500

**Propósito:** Gestión de productos y categorías del e-commerce.

---

### 3.2 Herramientas de Observabilidad

#### **Zipkin (Distributed Tracing)**

**Imagen Docker:** `openzipkin/zipkin:latest`

**Puerto:** 9411

**Propósito:** Trazabilidad distribuida de peticiones entre microservicios.

**Configuración en servicios:**

```yaml
spring:
  zipkin:
    base-url: ${SPRING_ZIPKIN_BASE_URL:http://localhost:9411/}
```

**Acceso:** http://localhost:9411

---

## 4. Configuración de CI/CD

### 4.1 GitHub Actions Workflows

Se implementaron **18 workflows** CI/CD automatizados:

| Servicio              | Workflow Dev                              | Workflow Stage                              | Workflow Prod                              |
| --------------------- | ----------------------------------------- | ------------------------------------------- | ------------------------------------------ |
| **user-service**      | `user-service-pipeline-dev-push.yml`      | `user-service-pipeline-stage-push.yml`      | `user-service-pipeline-prod-push.yml`      |
| **order-service**     | `order-service-pipeline-dev-push.yml`     | `order-service-pipeline-stage-push.yml`     | `order-service-pipeline-prod-push.yml`     |
| **product-service**   | `product-service-pipeline-dev-push.yml`   | `product-service-pipeline-stage-push.yml`   | `product-service-pipeline-prod-push.yml`   |
| **api-gateway**       | `api-gateway-pipeline-dev-push.yml`       | `api-gateway-pipeline-stage-push.yml`       | `api-gateway-pipeline-prod-push.yml`       |
| **service-discovery** | `service-discovery-pipeline-dev-push.yml` | `service-discovery-pipeline-stage-push.yml` | `service-discovery-pipeline-prod-push.yml` |
| **cloud-config**      | `cloud-config-pipeline-dev-push.yml`      | `cloud-config-pipeline-stage-push.yml`      | `cloud-config-pipeline-prod-push.yml`      |

### 4.2 Estructura de un Workflow CI/CD

**Ejemplo:** `user-service-pipeline-dev-push.yml`

```yaml
name: Dev - CI/CD user-service

on:
  push:
    branches: [dev]
  pull_request:
    branches: [dev]

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: "11"
          distribution: "temurin"
          cache: "maven"

      - name: Build with Maven (user-service only)
        run: ./mvnw -B -DskipTests package -pl user-service -am

      - name: Run tests
        run: ./mvnw test -pl user-service

      - name: Login to Docker Hub
        if: github.event_name == 'push'
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Build and push Docker image
        if: github.event_name == 'push'
        run: |
          cd user-service
          docker build -t ${{ secrets.DOCKERHUB_USERNAME }}/user-service-ecommerce-boot:dev .
          docker push ${{ secrets.DOCKERHUB_USERNAME }}/user-service-ecommerce-boot:dev
```

### 4.3 Secrets Configurados en GitHub

| Secret Name          | Descripción                                                          |
| -------------------- | -------------------------------------------------------------------- |
| `DOCKERHUB_USERNAME` | Usuario de Docker Hub (`rafaelaruiz`)                                |
| `DOCKERHUB_TOKEN`    | Token de acceso de Docker Hub                                        |
| `AZURE_CREDENTIALS`  | Credenciales del Service Principal de Azure (para despliegue futuro) |

### 4.4 Environments con Protección

| Environment   | Branch   | Aprobación Requerida | Reviewers   |
| ------------- | -------- | -------------------- | ----------- |
| `development` | `dev`    | ❌ No                | N/A         |
| `staging`     | `stage`  | ✅ Sí                | 1 revisor   |
| `production`  | `master` | ✅ Sí                | 2 revisores |

---

## 5. Proceso de Despliegue

### 5.1 Despliegue Local con Docker Compose

**Archivo:** `compose.yml`

**Comandos de despliegue:**

```powershell
# 1. Levantar todos los servicios
docker-compose up -d

# 2. Verificar estado
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 3. Ver logs
docker-compose logs -f

# 4. Detener servicios
docker-compose down
```

**Servicios incluidos:**

- **zipkin-container** (9411) - Distributed Tracing
- **service-discovery-container** (8761) - Eureka Server
- **cloud-config-container** (9296) - Configuration Server
- **api-gateway-container** (8080) - API Gateway
- **user-service-container** (8700) - User Management
- **order-service-container** (8300) - Order Management
- **product-service-container** (8500) - Product Management

**Variables de entorno configuradas:**

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=dev
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/
  - SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411
```

### 5.2 Configuración por Environment (Spring Profiles)

Cada microservicio tiene archivos de configuración separados por environment:

```
user-service/src/main/resources/
├── application.yml              # Configuración base
├── application-dev.yml          # Development
├── application-stage.yml        # Staging
└── application-prod.yml         # Production
```

**Ejemplo: application-dev.yml**

```yaml
spring:
  profiles:
    active: dev

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://localhost:8761/eureka/}

logging:
  level:
    root: DEBUG
```

**Ejemplo: application-prod.yml**

```yaml
spring:
  profiles:
    active: prod

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://service-discovery.production.svc.cluster.local:8761/eureka/}

logging:
  level:
    root: WARN
```

### 5.3 Despliegue en Kubernetes (Azure AKS)

**Ubicación IaC:** `infra/terraform/`

**Workflow de infraestructura:** `.github/workflows/infra-deploy-azure.yml`

**Manifiestos Kubernetes:** `infra/k8s/`

---

## 6. Pruebas y Validación

### 6.1 Verificación de Eureka Dashboard

**URL:** http://localhost:8761

**Servicios registrados:**

| Service Name    | Status | Availability Zones             |
| --------------- | ------ | ------------------------------ |
| API-GATEWAY     | UP (1) | localhost:api-gateway:8080     |
| USER-SERVICE    | UP (1) | localhost:user-service:8700    |
| ORDER-SERVICE   | UP (1) | localhost:order-service:8300   |
| PRODUCT-SERVICE | UP (1) | localhost:product-service:8500 |
| CLOUD-CONFIG    | UP (1) | localhost:cloud-config:9296    |

### 6.2 Pruebas de Health Checks

**API Gateway:**

```powershell
curl http://localhost:8080/actuator/health
```

**Respuesta esperada:**

```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "apiGateway": {
          "status": "UP",
          "failureRate": "0.0%",
          "slowCallRate": "0.0%",
          "failedCalls": 0,
          "notPermittedCalls": 0,
          "state": "CLOSED"
        }
      }
    },
    "discoveryComposite": {
      "status": "UP",
      "components": {
        "discoveryClient": {
          "status": "UP",
          "details": {
            "services": [
              "api-gateway",
              "cloud-config",
              "product-service",
              "user-service",
              "order-service"
            ]
          }
        }
      }
    }
  }
}
```

**User Service (a través del Gateway):**

```powershell
curl http://localhost:8080/user-service/actuator/health
```

### 6.3 Pruebas de Comunicación entre Microservicios

```powershell
# User Service
curl http://localhost:8700/user-service/actuator/health

# Order Service
curl http://localhost:8300/order-service/actuator/health

# Product Service
curl http://localhost:8500/product-service/actuator/health
```

---

## 7. Monitoreo y Trazabilidad

### 7.1 Zipkin Dashboard

**URL:** http://localhost:9411

**Funcionalidades:**

1. **Run Query:** Buscar trazas de peticiones
2. **Filter by Service:** Filtrar por servicio específico
3. **Timeline View:** Ver duración de cada span
4. **Dependencies:** Ver dependencias entre servicios

**Generar trazas de prueba:**

```powershell
# Hacer varias peticiones para generar trazas
for ($i=0; $i -lt 10; $i++) {
    curl http://localhost:8080/user-service/actuator/health
    Start-Sleep -Seconds 1
}
```

**Ejemplo de traza:**

```
api-gateway (20ms)
  └─ user-service (125ms)
     ├─ Database query (80ms)
     └─ Business logic (45ms)

Total: 145ms
```

### 7.2 Métricas de Prometheus

**Endpoint:** http://localhost:8080/actuator/prometheus

**Métricas expuestas:**

```
# Circuit Breaker Metrics
resilience4j_circuitbreaker_state{name="apiGateway",state="closed"} 1.0
resilience4j_circuitbreaker_failure_rate{name="apiGateway"} 0.0

# HTTP Metrics
http_server_requests_seconds_count{method="GET",status="200",uri="/actuator/health"} 150.0
http_server_requests_seconds_sum{method="GET",status="200",uri="/actuator/health"} 2.5

# JVM Metrics
jvm_memory_used_bytes{area="heap"} 256000000.0
jvm_gc_pause_seconds_count{action="end of minor GC"} 10.0
```

---

## 8. Conclusiones y Resultados

### 8.1 Logros Alcanzados

#### ✅ **Arquitectura de Microservicios**

- Implementación de **7 microservicios independientes**
- Separación de responsabilidades por dominio (Users, Orders, Products, etc.)
- Comunicación asíncrona con patrones de resiliencia

#### ✅ **Contenedorización**

- **Dockerfiles optimizados** con:
  - Imagen base ligera (`eclipse-temurin:11-jre-jammy`)
  - Usuario no-root para seguridad
  - Health checks nativos con curl
  - Variables de entorno para configuración dinámica

#### ✅ **CI/CD Automatizado**

- **18 workflows** de GitHub Actions
- **Build automático** en cada push
- **Testing automático** con JUnit
- **Push a Docker Hub** con estrategia de tags
- **Environments protegidos** con aprobaciones manuales

#### ✅ **Service Discovery**

- **Eureka Server** como registro centralizado
- **Auto-registro** de todos los microservicios
- **Health checks periódicos**
- **Failover automático**

#### ✅ **Observabilidad**

- **Distributed Tracing** con Zipkin
- **Métricas de Prometheus** expuestas
- **Logs centralizados** con Docker Compose
- **Circuit Breaker** para resiliencia

#### ✅ **Infraestructura como Código**

- **Terraform** para Azure AKS
- **Kubernetes manifests** con deployments, services y configmaps
- **Auto-scaling** configurado (1-5 nodos)
- **LoadBalancer** para acceso externo

### 8.2 Lecciones Aprendidas

#### **1. Importancia de las Variables de Entorno**

**Problema:** `cloud-config` no se registraba en Eureka porque usaba `localhost:8761`.

**Solución:**

```yaml
environment:
  - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/
```

**Lección:** En entornos contenerizados, **siempre usar nombres de servicios** en lugar de `localhost`.

#### **2. Health Checks con Curl**

**Problema:** Imágenes JRE no incluyen `curl` por defecto.

**Solución:**

```dockerfile
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
```

**Lección:** **Instalar dependencias explícitamente** en Dockerfiles.

#### **3. Estrategia de Tags Docker**

**Decisión:** Usar tags semánticos (`:dev`, `:stage`, `:latest`).

**Beneficios:**

- Identificación clara del ambiente
- Rollback sencillo en caso de fallos
- Compatibilidad con Kubernetes

#### **4. Orden de Inicio de Servicios**

**Solución:**

```yaml
depends_on:
  - service-discovery-container
  - zipkin-container
```

**Lección:** Usar `depends_on` en Docker Compose para garantizar orden de inicio.

### 8.3 Métricas del Proyecto

| Métrica                          | Valor                              |
| -------------------------------- | ---------------------------------- |
| **Microservicios implementados** | 7                                  |
| **Workflows CI/CD**              | 18                                 |
| **Imágenes Docker publicadas**   | 18 (6 servicios × 3 tags)          |
| **Tiempo promedio de build**     | 2-3 minutos por servicio           |
| **Tiempo de startup local**      | ~60 segundos (todos los servicios) |

### 8.4 Próximos Pasos Recomendados

#### **Corto Plazo (1-2 semanas)**

1. ✅ Desplegar en Azure AKS usando Terraform
2. ✅ Configurar monitoring avanzado con Prometheus + Grafana
3. ✅ Implementar API Rate Limiting en el Gateway
4. ✅ Agregar autenticación JWT con Spring Security

#### **Mediano Plazo (1-2 meses)**

1. ✅ Migrar a bases de datos persistentes (PostgreSQL/MySQL)
2. ✅ Implementar Event-Driven Architecture con Kafka
3. ✅ Agregar Circuit Breaker Dashboard (Hystrix/Resilience4j)
4. ✅ Implementar API Versioning en endpoints

#### **Largo Plazo (3-6 meses)**

1. ✅ Service Mesh con Istio o Linkerd
2. ✅ Chaos Engineering con Chaos Monkey
3. ✅ Performance Testing con JMeter/Gatling
4. ✅ Multi-region deployment con Azure Traffic Manager

---

## 9. Anexos

### Anexo A: Comandos Útiles

#### **Docker Compose**

```powershell
# Levantar servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Reiniciar un servicio específico
docker-compose restart user-service-container

# Ver estado de servicios
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Detener y eliminar contenedores
docker-compose down
```

#### **Maven**

```powershell
# Compilar todos los módulos
./mvnw clean package -DskipTests

# Compilar un módulo específico
./mvnw -B -DskipTests package -pl user-service -am

# Ejecutar tests
./mvnw test -pl user-service
```

#### **Kubernetes (para futuro deployment)**

```powershell
# Aplicar manifiestos
kubectl apply -f infra/k8s/ --recursive

# Ver pods
kubectl get pods

# Ver servicios
kubectl get services

# Ver logs de un pod
kubectl logs -f <pod-name>

# Port-forward para acceso local
kubectl port-forward service/api-gateway 8080:8080
```

### Anexo B: Troubleshooting

#### **Problema: Servicio no se registra en Eureka**

```powershell
# 1. Verificar logs del servicio
docker-compose logs -f user-service-container

# 2. Buscar errores de conexión
# Línea esperada:
DiscoveryClient_USER-SERVICE - registration status: 204

# 3. Verificar variables de entorno
docker inspect user-service-container | Select-String "EUREKA"
```

#### **Problema: Imágenes Docker no se actualizan**

```powershell
# 1. Forzar pull de imágenes
docker-compose pull

# 2. Reconstruir contenedores
docker-compose up -d --force-recreate --build

# 3. Limpiar imágenes antiguas
docker image prune -a
```

#### **Problema: Puerto ya en uso**

```powershell
# 1. Identificar proceso usando el puerto
netstat -ano | findstr :8080

# 2. Detener contenedores
docker-compose down

# 3. Liberar el puerto (Windows)
Stop-Process -Id <PID> -Force
```

### Anexo C: Estructura del Proyecto

```
ecommerce-microservice-backend-app/
├── .github/
│   └── workflows/              # 18 workflows CI/CD
│       ├── user-service-pipeline-dev-push.yml
│       ├── user-service-pipeline-stage-push.yml
│       ├── user-service-pipeline-prod-push.yml
│       └── ...
├── infra/
│   ├── k8s/                    # Kubernetes manifests
│   │   ├── zipkin/
│   │   ├── service-discovery/
│   │   ├── cloud-config/
│   │   ├── api-gateway/
│   │   ├── user-service/
│   │   ├── order-service/
│   │   └── product-service/
│   ├── terraform/              # Terraform IaC
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   ├── outputs.tf
│   │   └── terraform.tfvars
│   ├── README.md
│   └── AZURE_SETUP.md
├── user-service/
│   ├── src/
│   │   └── main/
│   │       └── resources/
│   │           ├── application.yml
│   │           ├── application-dev.yml
│   │           ├── application-stage.yml
│   │           └── application-prod.yml
│   ├── Dockerfile
│   └── pom.xml
├── order-service/
├── product-service/
├── api-gateway/
├── service-discovery/
├── cloud-config/
├── compose.yml                 # Docker Compose
├── pom.xml                     # Parent POM
├── README.md
├── TALLER2_GUIDE.md           # Guía del taller
└── TALLER2_DOCUMENTATION.md   # Este documento
```

---

**Documentación generada:** Noviembre 2025  
**Autor:** Rafaela Ruiz  
**Proyecto:** Ecommerce Microservices Backend  
**Repositorio:** GitHub - ecommerce-microservice-backend-app  
**Branch actual:** infra
