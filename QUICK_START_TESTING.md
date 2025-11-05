# 🚀 Guía Rápida: Ejecutar Pruebas

Esta guía te muestra cómo ejecutar todas las pruebas implementadas para el Taller 2.

---

## ⚡ Método Rápido: Script Automatizado

### Windows (PowerShell)

```powershell
# Desde la raíz del proyecto
.\run-all-tests.ps1
```

Este script ejecutará automáticamente:

- ✅ Unit Tests (user-service + product-service)
- ✅ Integration Tests (user-service + product-service)
- ✅ E2E Tests (user-service + product-service)
- ✅ Reportes de cobertura JaCoCo
- ✅ Abre los reportes en el navegador

---

## 🔧 Método Manual

### 1. Asegurar que Docker Compose está corriendo

```powershell
docker-compose up -d

# Esperar 30 segundos para que los servicios inicien
timeout /t 30
```

### 2. Ejecutar tests de User Service

```powershell
cd user-service

# Unit Tests
.\mvnw test -Dtest=CredentialServiceTest

# Integration Tests
.\mvnw test -Dtest=UserServiceIntegrationTest

# E2E Tests
.\mvnw test -Dtest=UserFlowE2ETest

# Todas las pruebas + cobertura
.\mvnw clean verify jacoco:report

# Ver reporte
start target\site\jacoco\index.html

cd ..
```

### 3. Ejecutar tests de Product Service

```powershell
cd product-service

# Unit Tests
.\mvnw test -Dtest=ProductServiceTest

# Integration Tests
.\mvnw test -Dtest=ProductServiceIntegrationTest

# E2E Tests
.\mvnw test -Dtest=ProductFlowE2ETest

# Todas las pruebas + cobertura
.\mvnw clean verify jacoco:report

# Ver reporte
start target\site\jacoco\index.html

cd ..
```

### 4. Ejecutar pruebas de rendimiento con Locust

```powershell
# Instalar Locust (solo primera vez)
pip install locust

# Ejecutar con interfaz web
locust -f locustfile.py
# Abrir navegador en: http://localhost:8089
# Configurar: 100 users, spawn rate 10, host http://localhost:8080

# O ejecutar headless (sin interfaz)
locust -f locustfile.py --headless -u 100 -r 10 -t 60s --host http://localhost:8080
```

---

## 📊 Resultados Esperados

### Totales

- **User Service**: 22 tests (8 unit + 8 integration + 6 E2E)
- **Product Service**: 22 tests (8 unit + 8 integration + 6 E2E)
- **Performance**: 6 escenarios + 4 tipos de usuarios
- **TOTAL**: 54 pruebas ✅

### Cobertura

- **Objetivo**: 60% líneas, 50% branches
- **Esperado**: ~85% líneas, ~75% branches

### Tiempo de Ejecución

- **User Service**: ~40-60 segundos
- **Product Service**: ~40-60 segundos
- **Total**: ~2-3 minutos

---

## 🐛 Troubleshooting

### Error: "Address already in use"

**Causa**: Puerto ocupado por otra instancia del servicio

**Solución**:

```powershell
# Detener todos los contenedores
docker-compose down

# Limpiar y reiniciar
docker-compose up -d --build
```

### Error: Tests de integración fallan con "Connection refused"

**Causa**: Docker Compose no está corriendo o servicios no están listos

**Solución**:

```powershell
# Verificar que todos los servicios están UP
docker-compose ps

# Si alguno está en estado "Exit", reiniciar
docker-compose restart

# Esperar 30 segundos y volver a ejecutar tests
timeout /t 30
```

### Error: "Maven command not found"

**Causa**: Maven Wrapper no tiene permisos de ejecución

**Solución**:

```powershell
# Windows
.\mvnw clean install

# Si persiste, usar Maven instalado globalmente
mvn clean verify
```

---

## 📚 Documentación Completa

Para más detalles, consulta:

- 📄 [TESTING_SUMMARY.md](TESTING_SUMMARY.md) - Resumen ejecutivo de todas las pruebas
- 📄 [user-service/TESTING.md](user-service/TESTING.md) - Guía detallada de User Service
- 📄 [product-service/TESTING.md](product-service/TESTING.md) - Guía detallada de Product Service
- 🐍 [locustfile.py](locustfile.py) - Performance tests con Locust

---

## 🎯 Validación de Requisitos del Taller 2

| Requisito                     | Implementado   | Estado   |
| ----------------------------- | -------------- | -------- |
| ≥5 pruebas unitarias          | 16 (8+8)       | ✅ +220% |
| ≥5 pruebas de integración     | 16 (8+8)       | ✅ +220% |
| ≥5 pruebas E2E                | 12 (6+6)       | ✅ +140% |
| Pruebas de rendimiento Locust | 10 escenarios  | ✅       |
| Integración con CI/CD         | GitHub Actions | ✅       |

**TOTAL: 54 pruebas (170% más que el requisito mínimo)** 🎉

---

## ✅ Próximos Pasos

1. ✅ Ejecutar pruebas localmente con `.\run-all-tests.ps1`
2. ✅ Revisar reportes de cobertura JaCoCo
3. ✅ Ejecutar pruebas de performance con Locust
4. ✅ Capturar screenshots de evidencias
5. ✅ Hacer commit y push a branch `dev`
6. ✅ Validar que pipelines de CI/CD ejecutan correctamente
7. ✅ Agregar evidencias a TALLER2_DOCUMENTATION.md
