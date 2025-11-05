# Guía Completa - Taller 2: Ecommerce Microservices

## 📋 Resumen del Taller

Este documento explica TODA la infraestructura y flujo de trabajo para el Taller 2.

### ✅ Lo que YA TIENES completado:

1. **Dockerfiles** - Todos los microservicios tienen Dockerfiles optimizados con curl para healthchecks
2. **GitHub Actions (reemplaza Jenkins)** - 18 workflows CI/CD (6 servicios × 3 ambientes: dev/stage/prod)
3. **Docker Hub** - Imágenes publicadas con tags `:dev`, `:stage`, `:latest`
4. **Docker Compose local** - Para pruebas locales
5. **Kubernetes manifests** - Deployments y Services para todos los microservicios
6. **Terraform** - Configuración para Azure AKS (aún no desplegado)

---

## 🎯 Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                     DESARROLLO LOCAL                             │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Docker Compose (compose.yml)                            │   │
│  │  ├─ Zipkin (openzipkin/zipkin)                          │   │
│  │  ├─ Eureka Server (service-discovery:8761)              │   │
│  │  ├─ Cloud Config (cloud-config:9296)                    │   │
│  │  ├─ API Gateway (api-gateway:8080)                      │   │
│  │  ├─ User Service (user-service:8700)                    │   │
│  │  ├─ Order Service (order-service:8300)                  │   │
│  │  └─ Product Service (product-service:8500)              │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     GITHUB ACTIONS (CI/CD)                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Push a branch → Workflow ejecuta:                      │   │
│  │  1. Checkout código                                     │   │
│  │  2. Setup Java 11 (Temurin)                             │   │
│  │  3. Maven build (./mvnw package)                        │   │
│  │  4. Maven test                                          │   │
│  │  5. Docker login (Docker Hub)                           │   │
│  │  6. Docker build                                        │   │
│  │  7. Docker push con tag correspondiente:                │   │
│  │     • dev → :dev                                        │   │
│  │     • stage → :stage                                    │   │
│  │     • master → :latest                                  │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      DOCKER HUB (Registry)                       │
│  rafaelaruiz/api-gateway-ecommerce-boot:dev                     │
│  rafaelaruiz/api-gateway-ecommerce-boot:stage                   │
│  rafaelaruiz/api-gateway-ecommerce-boot:latest                  │
│  (... same for all 6 services)                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│          AZURE KUBERNETES SERVICE (AKS) - FUTURO                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  Terraform crea:                                         │   │
│  │  • Resource Group                                        │   │
│  │  • AKS Cluster (2 nodos Standard_D2s_v3)                │   │
│  │  • Networking                                            │   │
│  │                                                           │   │
│  │  Kubectl aplica manifests:                               │   │
│  │  • infra/k8s/zipkin/deployment.yaml                     │   │
│  │  • infra/k8s/service-discovery/deployment.yaml          │   │
│  │  • infra/k8s/cloud-config/deployment.yaml               │   │
│  │  • infra/k8s/api-gateway/deployment.yaml                │   │
│  │  • infra/k8s/user-service/deployment.yaml               │   │
│  │  • infra/k8s/order-service/deployment.yaml              │   │
│  │  • infra/k8s/product-service/deployment.yaml            │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flujo de trabajo completo: Código → Producción

### **Escenario 1: Desarrollo (branch `dev`)**

```bash
# 1. Desarrollador hace cambios en código
git checkout dev
# ... haces cambios en user-service ...
git add .
git commit -m "Add new user validation"
git push origin dev

# 2. GitHub Actions SE EJECUTA AUTOMÁTICAMENTE
# - Workflow: .github/workflows/user-service-pipeline-dev-push.yml
# - Compila con Maven
# - Ejecuta tests
# - Build Docker image
# - Push a Docker Hub con tag :dev
#   Imagen: rafaelaruiz/user-service-ecommerce-boot:dev

# 3a. OPCIÓN LOCAL: Probar localmente
docker-compose pull  # Descarga imagen nueva con tag :dev
docker-compose up -d user-service-container

# 3b. OPCIÓN KUBERNETES (FUTURO): Deploy a AKS dev
kubectl set image deployment/user-service \
  user-service=rafaelaruiz/user-service-ecommerce-boot:dev
```

### **Escenario 2: Staging (branch `stage`)**

```bash
# 1. Hacer PR de dev → stage
# GitHub: Create Pull Request from dev to stage

# 2. Aprobar PR (requiere 1 reviewer según environment protection)
# GitHub: Approve and Merge

# 3. GitHub Actions SE EJECUTA AUTOMÁTICAMENTE
# - Workflow: .github/workflows/user-service-pipeline-stage-push.yml
# - Requiere aprobación manual (environment: staging)
# - Build y push con tag :stage
#   Imagen: rafaelaruiz/user-service-ecommerce-boot:stage

# 4. Deploy a AKS staging (FUTURO)
kubectl set image deployment/user-service \
  user-service=rafaelaruiz/user-service-ecommerce-boot:stage --namespace=staging
```

### **Escenario 3: Production (branch `master`)**

```bash
# 1. Hacer PR de stage → master
# GitHub: Create Pull Request from stage to master

# 2. Aprobar PR (requiere 2 reviewers según environment protection)
# GitHub: Approve and Merge

# 3. GitHub Actions SE EJECUTA AUTOMÁTICAMENTE
# - Workflow: .github/workflows/user-service-pipeline-prod-push.yml
# - Requiere aprobación manual (environment: production)
# - Build y push con tag :latest
#   Imagen: rafaelaruiz/user-service-ecommerce-boot:latest

# 4. Deploy a AKS production (FUTURO)
kubectl set image deployment/user-service \
  user-service=rafaelaruiz/user-service-ecommerce-boot:latest --namespace=production
```

---

## 📦 Docker Compose vs Kubernetes

### **Docker Compose (Local - LO QUE USAS AHORA)**

**Archivo:** `compose.yml`

```yaml
services:
  zipkin-container:
    image: openzipkin/zipkin
    ports:
      - 9411:9411

  service-discovery-container:
    image: ${DOCKERHUB_USERNAME}/service-discovery-ecommerce-boot:dev
    ports:
      - 8761:8761
    environment:
      - SPRING_PROFILES_ACTIVE=dev

  api-gateway-container:
    image: ${DOCKERHUB_USERNAME}/api-gateway-ecommerce-boot:dev
    ports:
      - 8080:8080
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery-container:8761/eureka/
      - SPRING_ZIPKIN_BASE_URL=http://zipkin-container:9411
    depends_on:
      - service-discovery-container
      - zipkin-container
```

**Comandos:**

```bash
# Levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f api-gateway-container

# Ver servicios corriendo
docker ps

# Parar todos los servicios
docker-compose down
```

**Ventajas:**

- ✅ Rápido para desarrollo local
- ✅ Fácil de configurar
- ✅ No requiere cluster externo
- ✅ Usa las imágenes de Docker Hub directamente

**Desventajas:**

- ❌ No escala automáticamente
- ❌ No tiene auto-healing (si un contenedor falla, no se reinicia solo)
- ❌ No simula producción real

### **Kubernetes (Producción - FUTURO en Azure AKS)**

**Archivo:** `infra/k8s/api-gateway/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 2 # Kubernetes crea 2 pods automáticamente
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
        - name: api-gateway
          image: rafaelaruiz/api-gateway-ecommerce-boot:dev
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "dev"
            - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
              value: "http://service-discovery:8761/eureka/"
            - name: SPRING_ZIPKIN_BASE_URL
              value: "http://zipkin:9411"
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
spec:
  type: LoadBalancer # Kubernetes crea IP pública automáticamente
  ports:
    - port: 80
      targetPort: 8080
  selector:
    app: api-gateway
```

**Comandos:**

```bash
# Aplicar manifests
kubectl apply -f infra/k8s/api-gateway/

# Ver pods
kubectl get pods

# Ver servicios
kubectl get services

# Ver logs
kubectl logs -f deployment/api-gateway

# Escalar manualmente
kubectl scale deployment api-gateway --replicas=5

# Actualizar imagen
kubectl set image deployment/api-gateway \
  api-gateway=rafaelaruiz/api-gateway-ecommerce-boot:stage
```

**Ventajas:**

- ✅ Auto-scaling (escala según CPU/memoria)
- ✅ Auto-healing (reinicia pods fallidos automáticamente)
- ✅ Rolling updates (actualiza sin downtime)
- ✅ Load balancing automático
- ✅ Simula producción real
- ✅ Múltiples ambientes (dev/staging/prod) en el mismo cluster

**Desventajas:**

- ❌ Más complejo de configurar
- ❌ Requiere cluster (local con Minikube o cloud con AKS)
- ❌ Cuesta dinero en cloud (AKS ~$150-200/mes)

---

## 🔍 ¿Cómo funcionan los tags en Docker?

### Concepto de Tags

Un **tag** es una etiqueta que identifica una versión específica de una imagen Docker:

```
rafaelaruiz/api-gateway-ecommerce-boot:dev
      ↑              ↑                      ↑
   usuario      nombre de imagen         tag
```

### Tags en tu proyecto:

| Tag       | Branch   | Propósito              | Ejemplo                                         |
| --------- | -------- | ---------------------- | ----------------------------------------------- |
| `:dev`    | `dev`    | Desarrollo activo      | `rafaelaruiz/api-gateway-ecommerce-boot:dev`    |
| `:stage`  | `stage`  | Pre-producción para QA | `rafaelaruiz/api-gateway-ecommerce-boot:stage`  |
| `:latest` | `master` | Producción estable     | `rafaelaruiz/api-gateway-ecommerce-boot:latest` |

### ¿Necesitas hacer pull cada vez?

**NO**, cuando usas Kubernetes o Docker Compose con la política `imagePullPolicy: Always`, el sistema **descarga automáticamente** la última versión de la imagen con ese tag.

**Ejemplo:**

```bash
# Subes código a branch dev
git push origin dev

# GitHub Actions ejecuta workflow y pushea imagen:
# rafaelaruiz/user-service-ecommerce-boot:dev (NUEVA VERSIÓN)

# En Kubernetes:
kubectl rollout restart deployment/user-service
# Kubernetes detecta que hay una imagen nueva con tag :dev
# y la descarga automáticamente SIN que hagas pull manual
```

---

## 🧪 Cómo probar que todo funciona (PASO A PASO)

### **Paso 1: Verificar que Docker Compose está corriendo**

```powershell
# Ver servicios
docker ps

# Deberías ver:
# - zipkin-container (9411)
# - service-discovery-container (8761)
# - cloud-config-container (9296)
# - api-gateway-container (8080)
# - user-service-container (8700)
# - order-service-container (8300)
# - product-service-container (8500)
```

### **Paso 2: Verificar Eureka Dashboard**

Abre en tu navegador: http://localhost:8761

Deberías ver todos los servicios registrados:

- API-GATEWAY
- USER-SERVICE
- ORDER-SERVICE
- PRODUCT-SERVICE
- CLOUD-CONFIG

### **Paso 3: Verificar Zipkin (Trazabilidad)**

Abre en tu navegador: http://localhost:9411

Haz una petición a un servicio:

```powershell
curl http://localhost:8080/actuator/health
```

Luego ve a Zipkin y haz clic en "Run Query". Deberías ver las trazas de la petición.

### **Paso 4: Probar comunicación entre microservicios**

```powershell
# Probar API Gateway → User Service
curl http://localhost:8080/api/users

# Probar API Gateway → Product Service
curl http://localhost:8080/api/products

# Probar API Gateway → Order Service
curl http://localhost:8080/api/orders
```

### **Paso 5: Ver logs de un servicio**

```powershell
# Ver logs de API Gateway
docker-compose logs -f api-gateway-container

# Ver logs de User Service
docker-compose logs -f user-service-container
```

---

## 🚀 Próximos pasos para el Taller

### Opción A: Continuar con desarrollo LOCAL (Recomendado para el taller)

```bash
# 1. Implementar patrones simples en el código
# 2. Hacer commits a branch dev
# 3. GitHub Actions buildeará y pusheará a Docker Hub
# 4. Hacer pull de la imagen actualizada:
docker-compose pull user-service-container
docker-compose up -d user-service-container

# 5. Probar cambios localmente con Zipkin y Eureka
# 6. Documentar resultados con screenshots
```

### Opción B: Desplegar a Azure Kubernetes (Opcional, costoso)

```bash
# 1. Configurar Azure CLI y crear Service Principal
# 2. Agregar AZURE_CREDENTIALS a GitHub Secrets
# 3. Push a branch infra para ejecutar Terraform
# 4. Verificar que cluster AKS fue creado
# 5. Aplicar manifests de Kubernetes
kubectl apply -f infra/k8s/
```

---

## 📊 Comparación: Local vs Kubernetes

| Aspecto            | Docker Compose (Local) | Kubernetes (AKS) |
| ------------------ | ---------------------- | ---------------- |
| **Costo**          | Gratis                 | ~$150-200/mes    |
| **Complejidad**    | Baja                   | Alta             |
| **Escalabilidad**  | Manual                 | Automática       |
| **Auto-healing**   | No                     | Sí               |
| **Load balancing** | No                     | Sí               |
| **Para el taller** | ✅ Suficiente          | ⚠️ Opcional      |

---

## 🎓 Conclusión para el Taller 2

**LO QUE YA TIENES:**

1. ✅ Microservicios funcionando con Docker Compose
2. ✅ GitHub Actions (reemplaza Jenkins)
3. ✅ CI/CD pipeline completo (dev/stage/prod)
4. ✅ Imágenes en Docker Hub
5. ✅ Service Discovery (Eureka)
6. ✅ Distributed Tracing (Zipkin)
7. ✅ Kubernetes manifests listos (para futuro)

**LO QUE FALTA (según requisitos del taller):**

- Implementar patrones simples en el código
- Documentar arquitectura y flujos
- Tomar screenshots de Eureka mostrando servicios registrados
- Tomar screenshots de Zipkin mostrando trazas
- Probar comunicación entre microservicios

**RECOMENDACIÓN:**
No necesitas desplegar a Azure AKS para cumplir con el taller. Docker Compose local es suficiente para demostrar:

- ✅ Microservicios comunicándose
- ✅ Service Discovery funcionando
- ✅ Distributed Tracing funcionando
- ✅ CI/CD con GitHub Actions
- ✅ Dockerización de microservicios

**Azure Kubernetes es OPCIONAL** y solo lo necesitas si:

- Quieres aprender Kubernetes en la nube
- Tienes créditos de Azure for Students
- El profesor requiere explícitamente deployment en cloud

---

## 📚 Referencias útiles

- **Docker Compose:** https://docs.docker.com/compose/
- **Kubernetes:** https://kubernetes.io/docs/
- **Eureka:** https://spring.io/projects/spring-cloud-netflix
- **Zipkin:** https://zipkin.io/
- **GitHub Actions:** https://docs.github.com/en/actions
- **Azure AKS:** https://docs.microsoft.com/en-us/azure/aks/

---

**Última actualización:** Noviembre 2025
