# ==============================================================================
# Script para ejecutar TODAS las pruebas del Taller 2 (FIXED VERSION)
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
    
    $allTestsPassed = $true
    
    try {
        # ============================================
        # Ejecutar TODOS los tests (unit + integration + E2E)
        # ============================================
        Write-Host "  [1/2] Ejecutando TODOS los Tests (Unit + Integration + E2E)..." -ForegroundColor Green
        
        & ..\mvnw clean verify 2>&1 | Tee-Object -Variable allTestsOutput | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-Host ""
            Write-Host "  [X] ALGUNOS TESTS FALLARON" -ForegroundColor Red
            Write-Host "  Ultimas lineas del log:" -ForegroundColor Yellow
            $allTestsOutput | Select-Object -Last 50 | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
            $allTestsPassed = $false
        } else {
            Write-Host "        [OK] Todos los Tests: PASSED" -ForegroundColor Green
        }
        Write-Host ""
        
        # ============================================
        # Generate JaCoCo Report
        # ============================================
        if ($allTestsPassed) {
            Write-Host "  [2/2] Generando reporte de cobertura JaCoCo..." -ForegroundColor Green
            & ..\mvnw jacoco:report -q 2>&1 | Out-Null
            Write-Host "        [OK] Reporte generado: target\site\jacoco\index.html" -ForegroundColor Green
            Write-Host ""
        }
        
        if ($allTestsPassed) {
            Write-Host "  [OK] RESULTADO: TODOS LOS TESTS PASARON PARA $ServiceName" -ForegroundColor Green -BackgroundColor DarkGreen
        } else {
            Write-Host "  [X] RESULTADO: ALGUNOS TESTS FALLARON PARA $ServiceName" -ForegroundColor Red -BackgroundColor DarkRed
        }
        Write-Host ""
        
        return $allTestsPassed
    }
    catch {
        Write-Host ""
        Write-Host "  [X] ERROR FATAL EN $ServiceName" -ForegroundColor Red -BackgroundColor DarkRed
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

if (-not (Test-Path "user-service") -or -not (Test-Path "product-service")) {
    Write-Host "[X] ERROR: Debes ejecutar este script desde la raiz del proyecto" -ForegroundColor Red
    Write-Host "Directorio actual: $PWD" -ForegroundColor Yellow
    exit 1
}

Write-Host "  Verificando que Docker Compose esta corriendo..." -ForegroundColor Gray
$dockerStatus = docker-compose ps -q 2>&1
if (-not $dockerStatus -or $dockerStatus -match "error") {
    Write-Host "  [!] Docker Compose no esta corriendo. Iniciando servicios..." -ForegroundColor Yellow
    docker-compose up -d
    Write-Host "  Esperando 40 segundos para que los servicios inicien..." -ForegroundColor Gray
    Start-Sleep -Seconds 40
} else {
    Write-Host "  [OK] Docker Compose esta corriendo correctamente" -ForegroundColor Green
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
    Write-Host "    [OK] User Service    - TODOS LOS TESTS PASARON (22 tests)" -ForegroundColor Green
} else {
    Write-Host "    [X]  User Service    - ALGUNOS TESTS FALLARON" -ForegroundColor Red
}

if ($productServiceSuccess) {
    Write-Host "    [OK] Product Service - TODOS LOS TESTS PASARON (22 tests)" -ForegroundColor Green
} else {
    Write-Host "    [X]  Product Service - ALGUNOS TESTS FALLARON" -ForegroundColor Red
}

Write-Host ""
Write-Host "  Tiempo total: $($duration.TotalSeconds.ToString('0.00')) segundos" -ForegroundColor White
Write-Host ""

# ==============================================================================
# ABRIR REPORTES
# ==============================================================================
if ($userServiceSuccess -or $productServiceSuccess) {
    Write-Host "  Reportes de Cobertura JaCoCo:" -ForegroundColor Cyan
    Write-Host ""
    
    if ($userServiceSuccess) {
        $userReportPath = "user-service\target\site\jacoco\index.html"
        if (Test-Path $userReportPath) {
            Write-Host "    User Service: $userReportPath" -ForegroundColor White
            Start-Process $userReportPath
        }
    }
    
    if ($productServiceSuccess) {
        $productReportPath = "product-service\target\site\jacoco\index.html"
        if (Test-Path $productReportPath) {
            Write-Host "    Product Service: $productReportPath" -ForegroundColor White
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
    Write-Host "  [OK] EXITO! TODOS LOS TESTS PASARON (44 tests)" -ForegroundColor Green -BackgroundColor DarkGreen
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Proximos pasos:" -ForegroundColor Cyan
    Write-Host "    1. Revisar reportes de cobertura" -ForegroundColor White
    Write-Host "    2. Ejecutar: locust -f locustfile.py" -ForegroundColor White
    Write-Host "    3. Hacer commit y push a dev" -ForegroundColor White
    Write-Host ""
    exit 0
} else {
    Write-Host "  [X] ALGUNOS TESTS FALLARON" -ForegroundColor Red -BackgroundColor DarkRed
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Revisa los logs arriba para identificar los errores." -ForegroundColor Yellow
    Write-Host ""
    exit 1
}
