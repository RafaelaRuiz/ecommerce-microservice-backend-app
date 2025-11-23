# 🧪 Guía de Pruebas - Product Service

Esta guía documenta todas las pruebas implementadas para el **Product Service** como parte del Taller 2.

---

## 📋 Resumen de Pruebas Implementadas

| Tipo de Prueba        | Cantidad | Archivo                              | Descripción                              |
| --------------------- | -------- | ------------------------------------ | ---------------------------------------- |
| **Unit Tests**        | 8        | `ProductServiceTest.java`            | Pruebas unitarias de la capa de servicio |
| **Integration Tests** | 8        | `ProductServiceIntegrationTest.java` | Pruebas de integración de API REST       |
| **E2E Tests**         | 6        | `ProductFlowE2ETest.java`            | Pruebas de flujos completos de producto  |

**Total: 22 pruebas** ✅ (Supera el requisito mínimo de 5 por categoría)

---

## 🔬 Pruebas Unitarias (Unit Tests)

### Descripción

Validan componentes individuales de la capa de servicio (`ProductService`) en completo aislamiento usando **Mockito** para simular dependencias.

### Archivo

`product-service/src/test/java/com/selimhorri/app/service/ProductServiceTest.java`

### Pruebas Implementadas

1. ✅ **testFindById_ShouldReturnProduct_WhenIdExists**

   - Valida que se retorna un producto cuando el ID existe

2. ✅ **testFindById_ShouldThrowException_WhenIdDoesNotExist**

   - Valida que se lanza excepción cuando el ID no existe

3. ✅ **testSave_ShouldPersistProduct_WhenValidData**

   - Valida que se persiste correctamente un producto válido

4. ✅ **testFindAll_ShouldReturnAllProducts**

   - Valida que se retornan todos los productos

5. ✅ **testUpdate_ShouldUpdateProduct_WhenValidData**

   - Valida actualización de producto existente

6. ✅ **testDeleteById_ShouldRemoveProduct_WhenIdExists**

   - Valida eliminación de producto por ID

7. ✅ **testFindAll_ShouldReturnImmutableList**

   - Valida que findAll retorna lista inmutable

8. ✅ **testFindAll_ShouldRemoveDuplicates**
   - Valida que se eliminan duplicados de la lista

### Ejecución

```powershell
# Ejecutar solo pruebas unitarias
cd product-service
.\mvnw test -Dtest=ProductServiceTest

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

`product-service/src/test/java/com/selimhorri/app/integration/ProductServiceIntegrationTest.java`

### Pruebas Implementadas

1. ✅ **testHealthEndpoint_ShouldReturnUp**

   - Valida `/actuator/health` retorna status UP

2. ✅ **testCreateProduct_ShouldReturn201Created**

   - Valida POST `/api/products` crea producto (201 Created)

3. ✅ **testGetProductById_ShouldReturn200OK**

   - Valida GET `/api/products/{id}` retorna producto (200 OK)

4. ✅ **testListAllProducts_ShouldReturnProductsList**

   - Valida GET `/api/products` retorna lista completa

5. ✅ **testUpdateProduct_ShouldReturn200OK**

   - Valida PUT `/api/products` actualiza producto (200 OK)

6. ✅ **testPrometheusMetrics_ShouldBeExposed**

   - Valida `/actuator/prometheus` expone métricas

7. ✅ **testApplicationInfo_ShouldBeExposed**

   - Valida `/actuator/info` retorna información de la app

8. ✅ **testDeleteProduct_ShouldReturn200OK**
   - Valida DELETE `/api/products/{id}` elimina producto (200 OK)

### Ejecución

```powershell
# Ejecutar solo pruebas de integración
cd product-service
.\mvnw test -Dtest=ProductServiceIntegrationTest

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

Validan flujos completos de gestión de productos simulando escenarios reales de uso del sistema.

### Archivo

`product-service/src/test/java/com/selimhorri/app/e2e/ProductFlowE2ETest.java`

### Flujos Implementados

1. ✅ **testE2E_ProductRegistration_CompleteFlow**

   - **Flujo:** Registro completo de nuevo producto
   - **Pasos:**
     1. Crear producto (POST)
     2. Verificar que existe (GET)
     3. Verificar que aparece en listado (GET all)

2. ✅ **testE2E_ProductUpdate_CompleteFlow**

   - **Flujo:** Actualización de producto
   - **Pasos:**
     1. Obtener producto existente
     2. Modificar datos
     3. Verificar persistencia de cambios

3. ✅ **testE2E_ProductSearchAndValidation_CompleteFlow**

   - **Flujo:** Búsqueda y validación de producto
   - **Pasos:**
     1. Buscar por ID
     2. Validar todos los campos
     3. Manejar caso de producto no encontrado

4. ✅ **testE2E_InventoryManagement_CompleteFlow**

   - **Flujo:** Gestión de inventario
   - **Pasos:**
     1. Verificar stock actual
     2. Simular venta (reducir stock)
     3. Verificar nuevo stock

5. ✅ **testE2E_ProductListing_CompleteFlow**

   - **Flujo:** Listado y búsqueda masiva
   - **Pasos:**
     1. Contar productos iniciales
     2. Crear 3 productos adicionales
     3. Verificar incremento en el contador

6. ✅ **testE2E_ProductDeletion_CompleteFlow**
   - **Flujo:** Eliminación de producto
   - **Pasos:**
     1. Verificar que producto existe
     2. Eliminar producto
     3. Confirmar 404 al buscar
     4. Verificar ausencia en listado

### Ejecución

```powershell
# Ejecutar solo pruebas E2E
cd product-service
.\mvnw test -Dtest=ProductFlowE2ETest

# IMPORTANTE: Todo el sistema debe estar corriendo
docker-compose up -d

# Los tests ejecutan en orden secuencial
# Observa los emojis en consola: 🚀 (inicio), ✓ (paso), ✅ (completado)
```

### Tecnologías

- **@SpringBootTest(RANDOM_PORT)** - Puerto aleatorio para evitar conflictos
- **@TestMethodOrder(OrderAnnotation.class)** - Orden secuencial
- **@Order(n)** - Especifica orden de ejecución
- **Static State** - Comparte estado entre tests (`createdProductId`)
- **Multi-step assertions** - Cada test tiene múltiples pasos

---

## 📊 Ejecución de Todas las Pruebas

### Todas las pruebas (Unit + Integration + E2E)

```powershell
cd product-service

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
cd product-service
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
cd product-service
.\mvnw test -Dtest=ProductServiceTest

# 3. Si pasa, ejecutar integration tests
.\mvnw test -Dtest=ProductServiceIntegrationTest

# 4. Si todo pasa, verificar cobertura
.\mvnw verify jacoco:report
start target\site\jacoco\index.html
```

### 2️⃣ Pre-Commit

```powershell
# Ejecutar todo antes de commit
cd product-service
.\mvnw clean verify

# Si todo pasa, hacer commit
git add .
git commit -m "feat: Implementar CRUD de productos con tests"
```

### 3️⃣ CI/CD Pipeline

El pipeline de GitHub Actions ejecuta:

```yaml
- name: Run Tests
  run: ./mvnw verify -pl product-service

- name: Generate Coverage Report
  run: ./mvnw jacoco:report -pl product-service
```

---

## 🎯 Checklist de Cumplimiento

### Requisitos del Taller 2

- ✅ **Al menos cinco nuevas pruebas unitarias** → **8 implementadas**
- ✅ **Al menos cinco nuevas pruebas de integración** → **8 implementadas**
- ✅ **Al menos cinco nuevas pruebas E2E** → **6 implementadas**
- ✅ **Integración con pipeline CI/CD** → **GitHub Actions configurado**

### Buenas Prácticas

- ✅ Tests con nombres descriptivos (`@DisplayName`)
- ✅ Uso de AssertJ para assertions fluidas
- ✅ Mocking adecuado con Mockito
- ✅ Tests aislados e independientes
- ✅ Configuración de test separada (`application-test.yml`)
- ✅ Reportes de cobertura con JaCoCo
- ✅ Separación clara: Unit / Integration / E2E

---

## 🐛 Troubleshooting

### Problema: Tests de integración fallan

**Causa:** El servicio no está corriendo

**Solución:**

```powershell
docker-compose up -d
timeout /t 10  # Esperar 10 segundos
cd product-service
.\mvnw test -Dtest=ProductServiceIntegrationTest
```

### Problema: JaCoCo report no se genera

**Causa:** No se ejecutaron los tests antes de generar reporte

**Solución:**

```powershell
cd product-service
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
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

---

## ✅ Conclusión

Este conjunto de pruebas proporciona:

1. **Cobertura completa** de la lógica de negocio (Unit Tests)
2. **Validación de integración** entre componentes (Integration Tests)
3. **Simulación de flujos reales** de productos (E2E Tests)

**Total: 22 pruebas** que superan ampliamente los requisitos del Taller 2. 🎉
