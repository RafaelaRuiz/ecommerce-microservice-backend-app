# 🧪 Guía de Pruebas - User Service

Esta guía documenta todas las pruebas implementadas para el **User Service** como parte del Taller 2.

---

## 📋 Resumen de Pruebas Implementadas

| Tipo de Prueba        | Cantidad | Archivo                           | Descripción                              |
| --------------------- | -------- | --------------------------------- | ---------------------------------------- |
| **Unit Tests**        | 8        | `CredentialServiceTest.java`      | Pruebas unitarias de la capa de servicio |
| **Integration Tests** | 8        | `UserServiceIntegrationTest.java` | Pruebas de integración de API REST       |
| **E2E Tests**         | 6        | `UserFlowE2ETest.java`            | Pruebas de flujos completos de usuario   |
| **Performance Tests** | 6        | `locustfile.py`                   | Pruebas de rendimiento y estrés          |

**Total: 28 pruebas** ✅ (Supera el requisito mínimo de 5 por categoría)

---

## 🔬 Pruebas Unitarias (Unit Tests)

### Descripción

Validan componentes individuales de la capa de servicio (`CredentialService`) en completo aislamiento usando **Mockito** para simular dependencias.

### Archivo

`user-service/src/test/java/com/selimhorri/app/service/CredentialServiceTest.java`

### Pruebas Implementadas

1. ✅ **testFindById_ShouldReturnCredential_WhenIdExists**

   - Valida que se retorna una credencial cuando el ID existe

2. ✅ **testFindById_ShouldThrowException_WhenIdDoesNotExist**

   - Valida que se lanza excepción cuando el ID no existe

3. ✅ **testSave_ShouldPersistCredential_WhenValidData**

   - Valida que se persiste correctamente una credencial válida

4. ✅ **testFindByUsername_ShouldReturnCredential_WhenUsernameExists**

   - Valida búsqueda por username cuando existe

5. ✅ **testFindByUsername_ShouldThrowException_WhenUsernameDoesNotExist**

   - Valida excepción cuando username no existe

6. ✅ **testFindAll_ShouldReturnAllCredentials**

   - Valida que se retornan todas las credenciales

7. ✅ **testUpdate_ShouldUpdateCredential_WhenValidData**

   - Valida actualización de credencial existente

8. ✅ **testDeleteById_ShouldRemoveCredential_WhenIdExists**
   - Valida eliminación de credencial por ID

### Ejecución

```powershell
# Ejecutar solo pruebas unitarias
cd user-service
.\mvnw test -Dtest=CredentialServiceTest

# Ver resultado en consola
```

### Tecnologías

- **JUnit 5** - Framework de testing
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **@Mock** - Simula repositorios
- **@InjectMocks** - Inyecta dependencias mockeadas

---

## 🔗 Pruebas de Integración (Integration Tests)

### Descripción

Validan la comunicación entre componentes reales (Controller → Service → Repository) y la correcta exposición de endpoints REST.

### Archivo

`user-service/src/test/java/com/selimhorri/app/integration/UserServiceIntegrationTest.java`

### Pruebas Implementadas

1. ✅ **testHealthEndpoint_ShouldReturnUp**

   - Valida `/actuator/health` retorna status UP

2. ✅ **testCreateCredential_ShouldReturn201Created**

   - Valida POST `/api/credentials` crea credencial (201 Created)

3. ✅ **testGetCredentialById_ShouldReturn200OK**

   - Valida GET `/api/credentials/{id}` retorna credencial (200 OK)

4. ✅ **testListAllCredentials_ShouldReturnCredentialsList**

   - Valida GET `/api/credentials` retorna lista completa

5. ✅ **testUpdateCredential_ShouldReturn200OK**

   - Valida PUT `/api/credentials` actualiza credencial (200 OK)

6. ✅ **testPrometheusMetrics_ShouldBeExposed**

   - Valida `/actuator/prometheus` expone métricas

7. ✅ **testApplicationInfo_ShouldBeExposed**

   - Valida `/actuator/info` retorna información de la app

8. ✅ **testDeleteCredential_ShouldReturn200OK**
   - Valida DELETE `/api/credentials/{id}` elimina credencial (200 OK)

### Ejecución

```powershell
# Ejecutar solo pruebas de integración
cd user-service
.\mvnw test -Dtest=UserServiceIntegrationTest

# IMPORTANTE: El servicio debe estar corriendo
# Opción 1: Ejecutar con Docker Compose
docker-compose up -d

# Opción 2: Ejecutar servicio localmente
.\mvnw spring-boot:run
```

### Tecnologías

- **@SpringBootTest** - Levanta contexto completo de Spring
- **TestRestTemplate** - Cliente HTTP para testing
- **@ActiveProfiles("test")** - Usa configuración de test
- **H2 Database** - Base de datos en memoria
- **@TestMethodOrder** - Orden secuencial de tests

---

## 🌐 Pruebas End-to-End (E2E Tests)

### Descripción

Validan flujos completos de usuarios simulando escenarios reales de uso del sistema de principio a fin.

### Archivo

`user-service/src/test/java/com/selimhorri/app/e2e/UserFlowE2ETest.java`

### Flujos Implementados

1. ✅ **testE2E_UserRegistration_CompleteFlow**

   - **Flujo:** Registro completo de nuevo usuario
   - **Pasos:**
     1. Crear credencial (POST)
     2. Verificar que existe (GET)
     3. Verificar que aparece en listado (GET all)

2. ✅ **testE2E_UserProfileUpdate_CompleteFlow**

   - **Flujo:** Actualización de perfil de usuario
   - **Pasos:**
     1. Obtener credencial existente
     2. Modificar datos
     3. Verificar persistencia de cambios

3. ✅ **testE2E_UserSearchAndValidation_CompleteFlow**

   - **Flujo:** Búsqueda y validación de usuario
   - **Pasos:**
     1. Buscar por ID
     2. Validar atributos de seguridad
     3. Manejar caso de usuario no encontrado

4. ✅ **testE2E_MultipleUsersManagement_CompleteFlow**

   - **Flujo:** Gestión de múltiples usuarios
   - **Pasos:**
     1. Contar usuarios iniciales
     2. Crear 3 usuarios nuevos
     3. Verificar incremento en el contador

5. ✅ **testE2E_UserDeletion_CompleteFlow**

   - **Flujo:** Eliminación de usuario
   - **Pasos:**
     1. Verificar que usuario existe
     2. Eliminar usuario
     3. Confirmar 404 al buscar
     4. Verificar ausencia en listado

6. ✅ **testE2E_SystemHealthValidation_CompleteFlow**
   - **Flujo:** Validación de salud del sistema
   - **Pasos:**
     1. Verificar health endpoint
     2. Verificar endpoints disponibles
     3. Verificar métricas de Prometheus

### Ejecución

```powershell
# Ejecutar solo pruebas E2E
cd user-service
.\mvnw test -Dtest=UserFlowE2ETest

# IMPORTANTE: Todo el sistema debe estar corriendo
docker-compose up -d

# Los tests ejecutan en orden secuencial
# Observa los emojis en consola: 🚀 (inicio), ✓ (paso), ✅ (completado)
```

### Tecnologías

- **@SpringBootTest(RANDOM_PORT)** - Puerto aleatorio para evitar conflictos
- **@TestMethodOrder(OrderAnnotation.class)** - Orden secuencial
- **@Order(n)** - Especifica orden de ejecución
- **Static State** - Comparte estado entre tests (`createdCredentialId`)
- **Multi-step assertions** - Cada test tiene múltiples pasos

---

## ⚡ Pruebas de Rendimiento (Performance Tests)

### Descripción

Validan el comportamiento del sistema bajo carga y estrés usando **Locust**, simulando múltiples usuarios concurrentes.

### Archivo

`locustfile.py` (raíz del proyecto)

### Escenarios Implementados

#### 1. **EcommerceUser** (Usuario Normal)

- 🟢 Health Check (peso: 5)
- 📋 Listar Credenciales (peso: 10 - más frecuente)
- ➕ Crear Credencial (peso: 2)
- 🔍 Obtener por ID (peso: 8)
- ✏️ Actualizar Credencial (peso: 1)
- 📊 Métricas Prometheus (peso: 3)

#### 2. **SpikingUser** (Picos de Carga)

- Hammering de Health Check
- Hammering de List Credentials
- Wait time: 0.1-0.5 segundos (muy agresivo)

#### 3. **ReadHeavyUser** (Lectura Pesada)

- Lectura intensiva de listados (peso: 15)
- Lectura intensiva por ID (peso: 10)

#### 4. **WriteHeavyUser** (Escritura Pesada)

- Escritura intensiva de creación (peso: 10)

### Ejecución

```powershell
# 1. Instalar Locust
pip install locust

# 2. Asegurar que el sistema está corriendo
docker-compose up -d

# 3. Opción A: Ejecutar con interfaz web
locust -f locustfile.py
# Abrir en navegador: http://localhost:8089
# Configurar:
#   - Number of users: 100
#   - Spawn rate: 10
#   - Host: http://localhost:8080

# 4. Opción B: Ejecutar headless (sin interfaz)
locust -f locustfile.py --headless -u 100 -r 10 -t 60s --host http://localhost:8080

# 5. Escenarios específicos:
# Solo usuarios normales:
locust -f locustfile.py --user-classes EcommerceUser

# Solo picos de carga:
locust -f locustfile.py --user-classes SpikingUser

# Solo lectura pesada:
locust -f locustfile.py --user-classes ReadHeavyUser

# Solo escritura pesada:
locust -f locustfile.py --user-classes WriteHeavyUser

# Mix personalizado:
locust -f locustfile.py --user-classes EcommerceUser SpikingUser
```

### Métricas Objetivo

| Métrica            | Objetivo     | Descripción                       |
| ------------------ | ------------ | --------------------------------- |
| **Latencia P50**   | < 100ms      | 50% de requests bajo 100ms        |
| **Latencia P95**   | < 500ms      | 95% de requests bajo 500ms        |
| **Latencia P99**   | < 1000ms     | 99% de requests bajo 1s           |
| **Throughput**     | > 100 req/s  | Mínimo 100 requests por segundo   |
| **Disponibilidad** | < 1% error   | Tasa de error menor al 1%         |
| **Concurrencia**   | 100 usuarios | Soportar 100 usuarios simultáneos |

---

## 📊 Ejecución de Todas las Pruebas

### Todas las pruebas (Unit + Integration + E2E)

```powershell
cd user-service

# Ejecutar todo con reporte de cobertura
.\mvnw clean verify jacoco:report

# Ver reporte de cobertura en navegador
start target\site\jacoco\index.html
```

### Solo Unit Tests

```powershell
.\mvnw test
```

### Solo Integration Tests

```powershell
.\mvnw verify -Dtest=*IntegrationTest
```

### Verificar cobertura mínima

```powershell
# Si la cobertura es menor a 60% líneas o 50% branches, fallará
.\mvnw verify jacoco:check
```

---

## 📈 Reporte de Cobertura con JaCoCo

### Generar Reporte

```powershell
cd user-service
.\mvnw clean test jacoco:report
```

### Ver Reporte

```powershell
# Abrir en navegador
start target\site\jacoco\index.html
```

### Interpretación del Reporte

El reporte JaCoCo muestra:

- **Verde**: Código cubierto por tests
- **Amarillo**: Código parcialmente cubierto
- **Rojo**: Código no cubierto

**Métricas:**

- **Líneas cubiertas**: Porcentaje de líneas ejecutadas
- **Branches cubiertos**: Porcentaje de ramas (if/else) ejecutadas
- **Complejidad ciclomática**: Complejidad del código

**Umbrales configurados:**

- Mínimo 60% cobertura de líneas
- Mínimo 50% cobertura de branches

---

## 🛠️ Configuración de Tests

### application-test.yml

Perfil de Spring Boot usado durante los tests:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # Base de datos en memoria
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recrea schema en cada test

eureka:
  client:
    enabled: false  # Deshabilita Eureka en tests

spring:
  zipkin:
    enabled: false  # Deshabilita Zipkin en tests
```

### pom.xml - Plugins

1. **Maven Surefire** - Ejecuta Unit Tests
2. **Maven Failsafe** - Ejecuta Integration/E2E Tests
3. **JaCoCo** - Genera reportes de cobertura

---

## 🚀 Flujo de Trabajo Recomendado

### 1️⃣ Desarrollo Local

```powershell
# 1. Escribir código
# 2. Ejecutar unit tests rápidos
cd user-service
.\mvnw test -Dtest=CredentialServiceTest

# 3. Si pasa, ejecutar integration tests
.\mvnw test -Dtest=UserServiceIntegrationTest

# 4. Si todo pasa, verificar cobertura
.\mvnw verify jacoco:report
start target\site\jacoco\index.html
```

### 2️⃣ Pre-Commit

```powershell
# Ejecutar todo antes de commit
cd user-service
.\mvnw clean verify

# Si todo pasa, hacer commit
git add .
git commit -m "feat: Implementar CRUD de credenciales con tests"
```

### 3️⃣ CI/CD Pipeline

El pipeline de GitHub Actions ejecuta:

```yaml
- name: Run Tests
  run: ./mvnw verify -pl user-service

- name: Generate Coverage Report
  run: ./mvnw jacoco:report -pl user-service

- name: Upload Coverage to Codecov
  uses: codecov/codecov-action@v3
```

### 4️⃣ Pruebas de Performance

```powershell
# Ejecutar después de deployment
# 1. Desplegar sistema
docker-compose up -d

# 2. Esperar a que esté ready
timeout /t 30

# 3. Ejecutar Locust
pip install locust
locust -f locustfile.py --headless -u 100 -r 10 -t 60s --host http://localhost:8080

# 4. Analizar resultados
# - Tasa de error < 1%
# - P95 < 500ms
# - Throughput > 100 req/s
```

---

## 📚 Estructura de Archivos

```
user-service/
├── src/
│   ├── main/java/com/selimhorri/app/
│   │   ├── service/
│   │   │   ├── CredentialService.java
│   │   │   └── impl/
│   │   │       └── CredentialServiceImpl.java  ← Código bajo test
│   │   └── ...
│   └── test/java/com/selimhorri/app/
│       ├── service/
│       │   └── CredentialServiceTest.java       ← 8 Unit Tests
│       ├── integration/
│       │   └── UserServiceIntegrationTest.java  ← 8 Integration Tests
│       └── e2e/
│           └── UserFlowE2ETest.java             ← 6 E2E Tests
└── pom.xml                                       ← Configuración Maven

locustfile.py                                     ← Performance Tests (raíz)
```

---

## 🎯 Checklist de Cumplimiento

### Requisitos del Taller 2

- ✅ **Al menos cinco nuevas pruebas unitarias** → **8 implementadas**
- ✅ **Al menos cinco nuevas pruebas de integración** → **8 implementadas**
- ✅ **Al menos cinco nuevas pruebas E2E** → **6 implementadas**
- ✅ **Pruebas de rendimiento con Locust** → **6 escenarios + 4 tipos de usuarios**

### Buenas Prácticas

- ✅ Tests con nombres descriptivos (`@DisplayName`)
- ✅ Uso de AssertJ para assertions fluidas
- ✅ Mocking adecuado con Mockito
- ✅ Tests aislados e independientes
- ✅ Configuración de test separada (`application-test.yml`)
- ✅ Reportes de cobertura con JaCoCo
- ✅ Separación clara: Unit / Integration / E2E
- ✅ Tests de performance con múltiples escenarios

---

## 🐛 Troubleshooting

### Problema: Tests de integración fallan

**Causa:** El servicio no está corriendo

**Solución:**

```powershell
docker-compose up -d
timeout /t 10  # Esperar 10 segundos
cd user-service
.\mvnw test -Dtest=UserServiceIntegrationTest
```

### Problema: Locust no encuentra el host

**Causa:** Docker Compose no está corriendo o puerto incorrecto

**Solución:**

```powershell
# Verificar servicios corriendo
docker-compose ps

# Verificar API Gateway en puerto 8080
curl http://localhost:8080/actuator/health

# Ejecutar Locust con host correcto
locust -f locustfile.py --host http://localhost:8080
```

### Problema: JaCoCo report no se genera

**Causa:** No se ejecutaron los tests antes de generar reporte

**Solución:**

```powershell
cd user-service
.\mvnw clean test jacoco:report
```

### Problema: H2 database conflicts en tests

**Causa:** Múltiples tests usando misma DB

**Solución:** Ya configurado en `application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

---

## 📞 Referencias

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Locust Documentation](https://docs.locust.io/en/stable/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

---

## ✅ Conclusión

Este conjunto de pruebas proporciona:

1. **Cobertura completa** de la lógica de negocio (Unit Tests)
2. **Validación de integración** entre componentes (Integration Tests)
3. **Simulación de flujos reales** de usuarios (E2E Tests)
4. **Evaluación de rendimiento** bajo carga (Performance Tests)

**Total: 28 pruebas** que superan ampliamente los requisitos del Taller 2. 🎉
