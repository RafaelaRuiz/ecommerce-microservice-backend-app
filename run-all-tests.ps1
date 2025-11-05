# ==============================================================================
# Script para ejecutar TODAS las pruebas del Taller 2
# ==============================================================================
# Este script ejecuta:
# 1. Unit Tests para user-service y product-service
# 2. Integration Tests para user-service y product-service
# 3. E2E Tests para user-service y product-service
# 4. Genera reportes de cobertura con JaCoCo
# 5. Abre los reportes en el navegador
# ==============================================================================

Write-Host ""
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  EJECUTANDO SUITE COMPLETA DE PRUEBAS - TALLER 2" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Continue"
$startTime = Get-Date

# ==============================================================================
# FUNCION: Ejecutar tests de un servicio
# ==============================================================================
function Run-ServiceTests {
    param(
        [string]$ServiceName,
        [string]$ServicePath
    )
    
    Write-Host "--------------------------------------------------------------------------------" -ForegroundColor Yellow
    Write-Host "  $ServiceName" -ForegroundColor Yellow
    Write-Host "--------------------------------------------------------------------------------" -ForegroundColor Yellow
    Write-Host ""
    
    Push-Location $ServicePath
    
    try {
        # Unit Tests
        Write-Host "  [1/4] Ejecutando Unit Tests..." -ForegroundColor Green
        if ($ServiceName -eq "USER SERVICE") {
            $unitTestClass = "CredentialServiceTest"
        } else {
            $unitTestClass = "ProductServiceTest"
        }
        
        & ..\mvnw test -Dtest=$unitTestClass -q
        if ($LASTEXITCODE -ne 0) {
            throw "Unit tests failed for $ServiceName"
        }
        Write-Host "        Unit Tests: PASSED" -ForegroundColor Green
        Write-Host ""
        
        # Integration Tests
        Write-Host "  [2/4] Ejecutando Integration Tests..." -ForegroundColor Green
        if ($ServiceName -eq "USER SERVICE") {
            $integrationTestClass = "UserServiceIntegrationTest"
        } else {
            $integrationTestClass = "ProductServiceIntegrationTest"
        }
        
        & ..\mvnw test -Dtest=$integrationTestClass -q
        if ($LASTEXITCODE -ne 0) {
            throw "Integration tests failed for $ServiceName"
        }
        Write-Host "        Integration Tests: PASSED" -ForegroundColor Green
        Write-Host ""
        
        # E2E Tests
        Write-Host "  [3/4] Ejecutando E2E Tests..." -ForegroundColor Green
        if ($ServiceName -eq "USER SERVICE") {
            $e2eTestClass = "UserFlowE2ETest"
        } else {
            $e2eTestClass = "ProductFlowE2ETest"
        }
        
        & ..\mvnw test -Dtest=$e2eTestClass -q
        if ($LASTEXITCODE -ne 0) {
            throw "E2E tests failed for $ServiceName"
        }
        Write-Host "        E2E Tests: PASSED" -ForegroundColor Green
        Write-Host ""
        
        # Generate JaCoCo Report
        Write-Host "  [4/4] Generando reporte de cobertura JaCoCo..." -ForegroundColor Green
        & ..\mvnw jacoco:report -q
        if ($LASTEXITCODE -ne 0) {
            throw "JaCoCo report generation failed for $ServiceName"
        }
        Write-Host "        Reporte generado: target\site\jacoco\index.html" -ForegroundColor Green
        Write-Host ""
        
        Write-Host "  RESULTADO: TODOS LOS TESTS PASARON PARA $ServiceName" -ForegroundColor Green -BackgroundColor DarkGreen
        Write-Host ""
        
        return $true
    }
    catch {
        Write-Host ""
        Write-Host "  ERROR EN $ServiceName" -ForegroundColor Red -BackgroundColor DarkRed
        Write-Host "  $($_.Exception.Message)" -ForegroundColor Red
        Write-Host ""
        return $false
    }
    finally {
        Pop-Location
    }
}

# ==============================================================================
# VALIDAR PRERREQUISITOS
# ==============================================================================
Write-Host "Validando prerrequisitos..." -ForegroundColor Cyan
Write-Host ""

# Verificar que estamos en el directorio correcto
if (-not (Test-Path "user-service") -or -not (Test-Path "product-service")) {
    Write-Host "ERROR: Debes ejecutar este script desde la raiz del proyecto" -ForegroundColor Red
    Write-Host "Directorio actual: $PWD" -ForegroundColor Yellow
    exit 1
}

# Verificar que Docker Compose esta corriendo (para integration/E2E tests)
Write-Host "  Verificando que Docker Compose esta corriendo..." -ForegroundColor Gray
try {
    $dockerStatus = docker-compose ps -q 2>&1
    if (-not $dockerStatus) {
        Write-Host "  Docker Compose no esta corriendo. Iniciando servicios..." -ForegroundColor Yellow
        docker-compose up -d
        Write-Host "  Esperando 30 segundos para que los servicios inicien..." -ForegroundColor Gray
        Start-Sleep -Seconds 30
    } else {
        Write-Host "  Docker Compose esta corriendo correctamente" -ForegroundColor Green
    }
}
catch {
    Write-Host "  No se pudo verificar Docker Compose. Los integration tests podrian fallar." -ForegroundColor Yellow
}

Write-Host ""

# ==============================================================================
# EJECUTAR TESTS
# ==============================================================================
$userServiceSuccess = Run-ServiceTests -ServiceName "USER SERVICE" -ServicePath "user-service"
$productServiceSuccess = Run-ServiceTests -ServiceName "PRODUCT SERVICE" -ServicePath "product-service"

# ==============================================================================
# RESUMEN FINAL
# ==============================================================================
$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  RESUMEN DE EJECUCION" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "  Servicios Probados:" -ForegroundColor White
if ($userServiceSuccess) {
    Write-Host "    [OK] User Service    - TODOS LOS TESTS PASARON" -ForegroundColor Green
} else {
    Write-Host "    [X]  User Service    - ALGUNOS TESTS FALLARON" -ForegroundColor Red
}

if ($productServiceSuccess) {
    Write-Host "    [OK] Product Service - TODOS LOS TESTS PASARON" -ForegroundColor Green
} else {
    Write-Host "    [X]  Product Service - ALGUNOS TESTS FALLARON" -ForegroundColor Red
}

Write-Host ""
Write-Host "  Tiempo total de ejecucion: $($duration.TotalSeconds.ToString('0.00')) segundos" -ForegroundColor White
Write-Host ""

# ==============================================================================
# ABRIR REPORTES DE COBERTURA
# ==============================================================================
if ($userServiceSuccess -or $productServiceSuccess) {
    Write-Host "  Reportes de Cobertura JaCoCo:" -ForegroundColor Cyan
    Write-Host ""
    
    if ($userServiceSuccess) {
        $userReportPath = "user-service\target\site\jacoco\index.html"
        if (Test-Path $userReportPath) {
            Write-Host "    User Service: $userReportPath" -ForegroundColor White
            Write-Host "    Abriendo en navegador..." -ForegroundColor Gray
            Start-Process $userReportPath
        }
    }
    
    if ($productServiceSuccess) {
        $productReportPath = "product-service\target\site\jacoco\index.html"
        if (Test-Path $productReportPath) {
            Write-Host "    Product Service: $productReportPath" -ForegroundColor White
            Write-Host "    Abriendo en navegador..." -ForegroundColor Gray
            Start-Process $productReportPath
        }
    }
    
    Write-Host ""
}

# ==============================================================================
# RESULTADO FINAL
# ==============================================================================
Write-Host "================================================================================" -ForegroundColor Cyan

if ($userServiceSuccess -and $productServiceSuccess) {
    Write-Host "  EXITO! TODOS LOS TESTS PASARON CORRECTAMENTE (54 tests)" -ForegroundColor Green -BackgroundColor DarkGreen
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Proximos pasos:" -ForegroundColor Cyan
    Write-Host "    1. Revisar reportes de cobertura en el navegador" -ForegroundColor White
    Write-Host "    2. Ejecutar pruebas de performance: locust -f locustfile.py" -ForegroundColor White
    Write-Host "    3. Hacer commit y push para ejecutar CI/CD pipeline" -ForegroundColor White
    Write-Host ""
    exit 0
} else {
    Write-Host "  ALGUNOS TESTS FALLARON" -ForegroundColor Red -BackgroundColor DarkRed
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Revisa los logs arriba para ver que tests fallaron." -ForegroundColor Yellow
    Write-Host ""
    exit 1
}
