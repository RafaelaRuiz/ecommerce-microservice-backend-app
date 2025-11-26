# Guía de Configuración de SonarQube

Esta guía explica cómo configurar SonarQube para que funcione en el pipeline de CI Quality.

## 📋 Requisitos Previos

1. **Servidor SonarQube** (una de las opciones):
   - SonarQube Cloud (gratis para proyectos open source): https://sonarcloud.io
   - SonarQube Server local o en la nube
   - SonarQube Community Edition (self-hosted)

## 🔧 Paso 1: Obtener los Valores Necesarios

### Opción A: SonarCloud (Recomendado para empezar)

1. Ve a https://sonarcloud.io y crea una cuenta (puedes usar tu cuenta de GitHub)
2. Crea una nueva organización (si no tienes una)
3. Crea un nuevo proyecto:
   - Selecciona "Analyze a new project"
   - Elige tu repositorio de GitHub
   - SonarCloud generará automáticamente un token
4. Obtén los valores:
   - **SONAR_HOST_URL**: `https://sonarcloud.io`
   - **SONAR_TOKEN**: 
     - Ve a: Tu perfil → Security → Generate Token
     - Copia el token generado (solo se muestra una vez)

### Opción B: SonarQube Server (Self-hosted)

1. Accede a tu servidor SonarQube (ej: `http://tu-servidor:9000`)
2. Inicia sesión
3. Crea un nuevo proyecto manualmente
4. Genera un token:
   - Ve a: My Account → Security → Generate Token
   - Copia el token
5. Obtén los valores:
   - **SONAR_HOST_URL**: `http://tu-servidor:9000` (o la URL de tu servidor)
   - **SONAR_TOKEN**: El token que generaste

## 🔐 Paso 2: Configurar GitHub Secrets

1. Ve a tu repositorio en GitHub
2. Navega a: **Settings** → **Secrets and variables** → **Actions**
3. Haz clic en **New repository secret**
4. Agrega los siguientes secrets:

   | Nombre del Secret | Valor | Ejemplo |
   |-------------------|-------|---------|
   | `SONAR_HOST_URL` | URL de tu servidor SonarQube | `https://sonarcloud.io` |
   | `SONAR_TOKEN` | Token de autenticación | `squ_xxxxxxxxxxxxxxxxxxxxx` |

5. Haz clic en **Add secret** para cada uno

## 🧪 Paso 3: Probar Localmente (Opcional)

Para probar SonarQube localmente antes de ejecutarlo en el pipeline:

### Opción 1: Usar variables de entorno

```bash
# Windows PowerShell (usando el wrapper de Maven)
$env:SONAR_HOST_URL="https://sonarcloud.io"
$env:SONAR_TOKEN="tu-token-aqui"
.\mvnw.cmd clean verify sonar:sonar

# Windows CMD (usando el wrapper de Maven)
set SONAR_HOST_URL=https://sonarcloud.io
set SONAR_TOKEN=tu-token-aqui
mvnw.cmd clean verify sonar:sonar

# Linux/Mac (usando el wrapper de Maven)
export SONAR_HOST_URL=https://sonarcloud.io
export SONAR_TOKEN=tu-token-aqui
./mvnw clean verify sonar:sonar

# Nota: Si tienes Maven instalado globalmente, puedes usar 'mvn' en lugar de './mvnw' o '.\mvnw.cmd'
```

### Opción 2: Crear archivo `.env` (no recomendado para tokens)

⚠️ **No commits el archivo `.env` con tokens** - solo para pruebas locales

Crea un archivo `.env` en la raíz del proyecto:
```env
SONAR_HOST_URL=https://sonarcloud.io
SONAR_TOKEN=tu-token-aqui
```

Luego ejecuta:
```bash
# Windows PowerShell
Get-Content .env | ForEach-Object { $line = $_; if ($line -match '^([^=]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process') } }
.\mvnw.cmd clean verify sonar:sonar

# Linux/Mac (requiere source o export)
source .env
./mvnw clean verify sonar:sonar
```

### Opción 3: Usar sonar-project.properties

Crea un archivo `sonar-project.properties` en la raíz del proyecto:

```properties
sonar.host.url=https://sonarcloud.io
sonar.login=tu-token-aqui
sonar.projectKey=tu-org_tu-repo
sonar.projectName=ecommerce-microservice-backend
sonar.sources=.
sonar.java.binaries=target/classes
```

⚠️ **IMPORTANTE**: Agrega `sonar-project.properties` al `.gitignore` si incluyes el token.

## ✅ Paso 4: Verificar en el Pipeline

Una vez configurados los secrets:

1. Haz un push a una rama que active el workflow (`dev`, `stage`, `master`)
2. O crea un PR hacia una de esas ramas
3. Ve a la pestaña **Actions** en GitHub
4. Verifica que el job `build-and-analyze` ejecute SonarQube sin errores
5. Revisa los resultados en tu servidor SonarQube/SonarCloud

## 🔍 Verificación

El pipeline ejecutará SonarQube si:
- ✅ Los secrets `SONAR_HOST_URL` y `SONAR_TOKEN` están configurados
- ✅ El plugin de SonarQube está en el `pom.xml` (ya agregado)
- ✅ El servidor SonarQube es accesible desde GitHub Actions

Si los secrets no están configurados, verás este mensaje en los logs:
```
SonarQube no configurado (faltan secretos SONAR_HOST_URL y SONAR_TOKEN)
```

## 📝 Notas Adicionales

- El token de SonarQube es sensible: **nunca lo commits al repositorio**
- Los tokens de SonarCloud no expiran, pero puedes revocarlos en cualquier momento
- Para proyectos privados en SonarCloud, necesitas un plan de pago
- El análisis se ejecuta automáticamente en cada push/PR a las ramas configuradas

## 🆘 Troubleshooting

**Error: "Authentication failed"**
- Verifica que el token sea correcto
- Asegúrate de que el token tenga permisos para el proyecto

**Error: "Project not found"**
- Verifica que el proyecto exista en SonarQube/SonarCloud
- El `projectKey` en el workflow convierte automáticamente `org/repo` a `org_repo` (reemplaza `/` por `_`)
- Si creaste el proyecto en SonarCloud desde GitHub, el projectKey debería ser: `tu-organizacion_tu-repositorio`
- Ejemplo: Si tu repo es `RafaelaRuiz/ecommerce-microservice-backend-app`, el projectKey será `RafaelaRuiz_ecommerce-microservice-backend-app`

**Error: "Connection refused"**
- Si usas SonarQube self-hosted, verifica que sea accesible desde internet
- Considera usar SonarCloud si no tienes un servidor público

