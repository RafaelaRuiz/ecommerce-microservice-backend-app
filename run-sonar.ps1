# Script para ejecutar análisis de SonarQube con variables de entorno desde .env
# Uso: .\run-sonar.ps1

# Verificar que existe el archivo .env
if (-not (Test-Path .env)) {
    Write-Host "Error: No se encontro el archivo .env" -ForegroundColor Red
    Write-Host "Por favor, crea un archivo .env con las siguientes variables:" -ForegroundColor Yellow
    Write-Host "  SONAR_HOST_URL=https://sonarcloud.io" -ForegroundColor Yellow
    Write-Host "  SONAR_TOKEN=tu-token-aqui" -ForegroundColor Yellow
    exit 1
}

# Cargar variables de entorno desde .env
Write-Host "Cargando variables de entorno desde .env..." -ForegroundColor Cyan
Get-Content .env | ForEach-Object {
    $line = $_.Trim()
    # Ignorar líneas vacías y comentarios
    if ($line -and -not $line.StartsWith('#')) {
        if ($line -match '^([^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, 'Process')
            Write-Host "  [OK] $key configurado" -ForegroundColor Green
        }
    }
}

# Verificar que las variables necesarias estén configuradas
if (-not $env:SONAR_HOST_URL) {
    Write-Host "Error: SONAR_HOST_URL no esta configurado en .env" -ForegroundColor Red
    exit 1
}

if (-not $env:SONAR_TOKEN) {
    Write-Host "Error: SONAR_TOKEN no esta configurado en .env" -ForegroundColor Red
    Write-Host "Por favor, agrega tu token de SonarCloud al archivo .env" -ForegroundColor Yellow
    exit 1
}

# SonarCloud requiere sonar.organization
# Si no está configurado, usar el valor por defecto
if (-not $env:SONAR_ORGANIZATION) {
    $env:SONAR_ORGANIZATION = "rafaelaruiz"
    Write-Host "  Usando organizacion por defecto: rafaelaruiz" -ForegroundColor Gray
} else {
    Write-Host "  Organizacion: $env:SONAR_ORGANIZATION" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Ejecutando analisis de SonarQube..." -ForegroundColor Cyan
Write-Host "  Host: $env:SONAR_HOST_URL" -ForegroundColor Gray
$tokenPreview = if ($env:SONAR_TOKEN.Length -gt 10) { $env:SONAR_TOKEN.Substring(0, 10) } else { $env:SONAR_TOKEN }
Write-Host "  Token: $tokenPreview..." -ForegroundColor Gray
Write-Host ""

# Obtener el projectKey (formato: org_repo)
# Si tienes git, usa el remoto, sino usa un valor por defecto
$projectKey = "RafaelaRuiz_ecommerce-microservice-backend-app"
try {
    $remoteUrl = git config --get remote.origin.url 2>$null
    if ($remoteUrl) {
        if ($remoteUrl -match 'github\.com[:/]([^/]+)/([^/]+?)(?:\.git)?$') {
            $org = $matches[1]
            $repo = $matches[2] -replace '\.git$', ''
            $projectKey = "${org}_${repo}"
        }
    }
} catch {
    # Si no hay git o falla, usa el valor por defecto
}

Write-Host "  Project Key: $projectKey" -ForegroundColor Gray
Write-Host ""

# Ejecutar Maven con SonarQube
$mavenArgs = @(
    "clean",
    "verify",
    "sonar:sonar",
    "-Dsonar.host.url=$env:SONAR_HOST_URL",
    "-Dsonar.login=$env:SONAR_TOKEN",
    "-Dsonar.projectKey=$projectKey",
    "-Dsonar.projectName=ecommerce-microservice-backend"
)

# Agregar sonar.organization si está configurado
if ($env:SONAR_ORGANIZATION) {
    $mavenArgs += "-Dsonar.organization=$env:SONAR_ORGANIZATION"
}

& .\mvnw.cmd $mavenArgs

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Analisis de SonarQube completado exitosamente!" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "El analisis de SonarQube fallo. Revisa los errores arriba." -ForegroundColor Red
    exit $LASTEXITCODE
}
