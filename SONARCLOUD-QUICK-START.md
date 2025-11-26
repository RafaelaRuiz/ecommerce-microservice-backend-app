# 🚀 Guía Rápida: Configurar SonarCloud

Esta guía te ayudará a configurar SonarCloud paso a paso.

## 📝 Paso 1: Crear cuenta y proyecto en SonarCloud

1. **Ve a SonarCloud:**
   - Abre https://sonarcloud.io en tu navegador

2. **Inicia sesión:**
   - Haz clic en **Log in**
   - Selecciona **Log in with GitHub**
   - Autoriza la aplicación

3. **Crea una organización (si es la primera vez):**
   - Si es tu primera vez, SonarCloud te pedirá crear una organización
   - Elige un nombre para tu organización (puede ser tu nombre de usuario de GitHub)
   - Selecciona el plan **Free** (gratis para proyectos open source)

4. **Crea un nuevo proyecto:**
   - Haz clic en **+** (arriba a la derecha) → **Analyze new project**
   - Selecciona **From GitHub**
   - Autoriza SonarCloud para acceder a tus repositorios si es necesario
   - Selecciona tu repositorio: `RafaelaRuiz/ecommerce-microservice-backend-app`
   - SonarCloud creará automáticamente el proyecto

## 🔑 Paso 2: Generar Token de Autenticación

1. **Ve a la configuración de seguridad:**
   - Haz clic en tu avatar (arriba a la derecha) → **My Account**
   - En el menú lateral, haz clic en **Security**

2. **Genera un nuevo token:**
   - En la sección **Generate Tokens**, escribe un nombre descriptivo (ej: `ecommerce-backend-local`)
   - Haz clic en **Generate**
   - ⚠️ **IMPORTANTE**: Copia el token inmediatamente, ya que solo se muestra una vez
   - El token tendrá un formato como: `squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

## 📁 Paso 3: Configurar archivo .env local

1. **Abre o crea el archivo `.env`** en la raíz del proyecto

2. **Agrega las siguientes líneas:**
   ```env
   SONAR_HOST_URL=https://sonarcloud.io
   SONAR_TOKEN=tu-token-generado-aqui
   ```

   Reemplaza `tu-token-generado-aqui` con el token que copiaste en el paso anterior.

3. **Verifica que el archivo `.env` esté en `.gitignore`** (ya debería estarlo)

## 🧪 Paso 4: Probar localmente

### Opción A: Usar el script helper (Más fácil)

```powershell
.\run-sonar.ps1
```

### Opción B: Ejecutar manualmente

```powershell
# Cargar variables de entorno
$env:SONAR_HOST_URL="https://sonarcloud.io"
$env:SONAR_TOKEN="tu-token-aqui"

# Ejecutar análisis
.\mvnw.cmd clean verify sonar:sonar
```

## ☁️ Paso 5: Configurar GitHub Secrets (Para el Pipeline)

Para que el pipeline de CI también ejecute SonarQube:

1. **Ve a tu repositorio en GitHub:**
   - Navega a: `https://github.com/RafaelaRuiz/ecommerce-microservice-backend-app`

2. **Ve a Settings:**
   - Haz clic en **Settings** (en la barra superior del repositorio)

3. **Ve a Secrets:**
   - En el menú lateral, haz clic en **Secrets and variables** → **Actions**

4. **Agrega los secrets:**
   - Haz clic en **New repository secret**
   - **Secret 1:**
     - Name: `SONAR_HOST_URL`
     - Value: `https://sonarcloud.io`
     - Haz clic en **Add secret**
   
   - **Secret 2:**
     - Haz clic en **New repository secret** nuevamente
     - Name: `SONAR_TOKEN`
     - Value: (el mismo token que usaste en el `.env`)
     - Haz clic en **Add secret**

## ✅ Verificación

### Verificar localmente:
- Ejecuta `.\run-sonar.ps1`
- Deberías ver un mensaje de éxito al final
- Ve a https://sonarcloud.io y verifica que el análisis aparezca en tu proyecto

### Verificar en el Pipeline:
- Haz un push a una rama `dev`, `stage`, o `master`
- O crea un PR hacia una de esas ramas
- Ve a la pestaña **Actions** en GitHub
- Verifica que el job `build-and-analyze` ejecute SonarQube sin errores

## 🆘 Troubleshooting

**Error: "Authentication failed"**
- Verifica que el token sea correcto
- Asegúrate de que no haya espacios extra en el token
- Genera un nuevo token si es necesario

**Error: "Project not found"**
- Verifica que el proyecto exista en SonarCloud
- El `projectKey` en el workflow usa `${{ github.repository }}` que debería ser `RafaelaRuiz_ecommerce-microservice-backend-app`
- Si el nombre del proyecto en SonarCloud es diferente, puedes ajustarlo en el workflow

**El script no encuentra el archivo .env**
- Asegúrate de que el archivo `.env` esté en la raíz del proyecto (mismo nivel que `pom.xml`)
- Verifica que el archivo no esté vacío

## 📚 Recursos Adicionales

- [Documentación oficial de SonarCloud](https://docs.sonarcloud.io/)
- [Guía completa de configuración](./SONARQUBE-SETUP.md)

