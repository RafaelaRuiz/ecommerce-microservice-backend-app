# Configuración de Azure para Despliegue

Este documento describe cómo configurar Azure y GitHub para el despliegue automatizado de la infraestructura.

## 📋 Prerequisitos

1. **Cuenta de Azure** con una suscripción activa
2. **Azure CLI** instalado localmente
3. **Terraform** instalado localmente (opcional para pruebas)
4. **kubectl** instalado localmente

## 🔧 Pasos de Configuración

### 1. Instalar Azure CLI

**Windows (PowerShell como Administrador):**

```powershell
# Opción 1: Con winget
winget install -e --id Microsoft.AzureCLI

# Opción 2: Con Chocolatey
choco install azure-cli

# Verificar instalación
az --version
```

**Documentación oficial:** https://docs.microsoft.com/en-us/cli/azure/install-azure-cli

### 2. Login en Azure

```powershell
# Login interactivo
az login

# Ver tu suscripción activa
az account show

# Listar todas tus suscripciones
az account list --output table

# (Opcional) Cambiar de suscripción
az account set --subscription "YOUR_SUBSCRIPTION_ID"

# Guardar tu Subscription ID para usarlo después
$SUBSCRIPTION_ID = (az account show --query id -o tsv)
echo $SUBSCRIPTION_ID
```

### 3. Crear Service Principal para Terraform y GitHub Actions

El Service Principal es una identidad que permite a Terraform y GitHub Actions interactuar con Azure.

```powershell
# Crear Service Principal con rol Contributor
az ad sp create-for-rbac `
  --name "terraform-ecommerce-sp" `
  --role Contributor `
  --scopes "/subscriptions/$SUBSCRIPTION_ID" `
  --sdk-auth

# IMPORTANTE: Guarda el output JSON completo que se muestra
# Lo necesitarás para GitHub Secrets
```

El comando anterior retornará algo como esto:

```json
{
  "clientId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "clientSecret": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "subscriptionId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "tenantId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
  "resourceManagerEndpointUrl": "https://management.azure.com/",
  "activeDirectoryGraphResourceId": "https://graph.windows.net/",
  "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
  "galleryEndpointUrl": "https://gallery.azure.com/",
  "managementEndpointUrl": "https://management.core.windows.net/"
}
```

**⚠️ GUARDA ESTE JSON COMPLETO DE FORMA SEGURA**

### 4. Configurar GitHub Secrets

Ve a tu repositorio en GitHub: `Settings` → `Secrets and variables` → `Actions` → `New repository secret`

Crea el siguiente secret:

| Secret Name         | Value                                                      |
| ------------------- | ---------------------------------------------------------- |
| `AZURE_CREDENTIALS` | El JSON completo del Service Principal (del paso anterior) |

### 5. Configurar Variables de Entorno Locales (para pruebas locales de Terraform)

Si quieres ejecutar Terraform localmente antes de usar GitHub Actions:

```powershell
# PowerShell
$env:ARM_CLIENT_ID = "clientId-del-service-principal"
$env:ARM_CLIENT_SECRET = "clientSecret-del-service-principal"
$env:ARM_SUBSCRIPTION_ID = "subscriptionId-de-tu-cuenta"
$env:ARM_TENANT_ID = "tenantId-de-tu-cuenta"

# Verificar
echo $env:ARM_SUBSCRIPTION_ID
```

### 6. Verificar Cuotas de Azure

Antes de crear el cluster AKS, verifica que tengas cuotas disponibles:

```powershell
# Ver cuotas de vCPUs en la región eastus
az vm list-usage --location eastus --output table

# Busca: "Standard DSv3 Family vCPUs" - necesitas al menos 4 vCPUs disponibles
```

Si no tienes suficientes cuotas, puedes:

- Cambiar la región en `terraform.tfvars` (ej: `westus2`, `westeurope`)
- Reducir el tamaño de VM en `terraform.tfvars` (ej: `Standard_B2s`)
- Solicitar aumento de cuota en Azure Portal

### 7. Personalizar la Configuración (Opcional)

Edita el archivo `infra/terraform/terraform.tfvars`:

```hcl
location            = "eastus"      # Cambia a tu región preferida
node_count          = 2             # Número inicial de nodos
vm_size             = "Standard_D2s_v3"  # Tamaño de las VMs
```

**Regiones comunes:**

- `eastus` - Estados Unidos Este
- `westus2` - Estados Unidos Oeste 2
- `westeurope` - Europa Occidental
- `southcentralus` - Estados Unidos Centro Sur

**Tamaños de VM comunes:**

- `Standard_B2s` - 2 vCPUs, 4GB RAM (económico, para desarrollo)
- `Standard_D2s_v3` - 2 vCPUs, 8GB RAM (recomendado para producción)
- `Standard_D4s_v3` - 4 vCPUs, 16GB RAM (para cargas más altas)

## 🚀 Despliegue

### Opción 1: Despliegue Automático con GitHub Actions

1. Hacer commit y push de los cambios a la rama `infra`:

```powershell
git add .
git commit -m "Add Azure infrastructure configuration"
git push origin infra
```

2. El workflow `infra-deploy-azure.yml` se ejecutará automáticamente
3. Requiere aprobación manual en el environment `staging`
4. Terraform creará el cluster AKS y desplegará las aplicaciones

### Opción 2: Despliegue Manual Local

```powershell
cd infra/terraform

# Inicializar Terraform
terraform init

# Ver el plan de ejecución
terraform plan

# Aplicar la configuración
terraform apply

# Obtener credenciales del cluster
az aks get-credentials `
  --resource-group ecommerce-microservices-rg `
  --name ecommerce-aks-cluster

# Verificar conexión
kubectl get nodes

# Desplegar aplicaciones
cd ../k8s
kubectl apply -f zipkin/
kubectl apply -f service-discovery/
kubectl apply -f cloud-config/
kubectl apply -f api-gateway/
kubectl apply -f user-service/
kubectl apply -f order-service/
kubectl apply -f product-service/

# Ver el estado
kubectl get pods
kubectl get services
```

## 📊 Verificación del Despliegue

```powershell
# Ver todos los pods
kubectl get pods

# Ver todos los servicios
kubectl get services

# Ver detalles del API Gateway (IP externa)
kubectl get service api-gateway

# Ver logs de un servicio
kubectl logs -f deployment/api-gateway

# Acceder a Eureka Dashboard (port-forward)
kubectl port-forward service/service-discovery 8761:8761
# Abrir: http://localhost:8761

# Acceder a Zipkin (port-forward)
kubectl port-forward service/zipkin 9411:9411
# Abrir: http://localhost:9411
```

## 💰 Estimación de Costos

**Cluster AKS con configuración por defecto:**

- 2 nodos Standard_D2s_v3
- ~$150-200 USD/mes

**Para reducir costos en desarrollo:**

- Usar `Standard_B2s` (más económico)
- Reducir a 1 nodo
- Detener el cluster cuando no lo uses:
  ```powershell
  az aks stop --resource-group ecommerce-microservices-rg --name ecommerce-aks-cluster
  az aks start --resource-group ecommerce-microservices-rg --name ecommerce-aks-cluster
  ```

## 🧹 Limpieza (Destruir Recursos)

```powershell
# Opción 1: Con Terraform
cd infra/terraform
terraform destroy

# Opción 2: Eliminar el Resource Group completo
az group delete --name ecommerce-microservices-rg --yes --no-wait
```

## 🔍 Troubleshooting

### Error: Insufficient quota

- Cambia la región en `terraform.tfvars`
- Reduce el `vm_size` o `node_count`
- Solicita aumento de cuota en Azure Portal

### Error: Service Principal authentication failed

- Verifica que `AZURE_CREDENTIALS` en GitHub Secrets esté correcto
- Verifica que el Service Principal tenga rol Contributor

### Pods en estado CrashLoopBackOff

```powershell
# Ver logs del pod
kubectl logs <pod-name>

# Describir el pod para ver eventos
kubectl describe pod <pod-name>
```

### No puedo acceder al API Gateway

```powershell
# Verificar que el LoadBalancer tenga IP externa
kubectl get service api-gateway

# Puede tomar 2-5 minutos en asignar la IP externa
```

## 📚 Referencias

- [Terraform Azure Provider](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)
- [Azure Kubernetes Service Documentation](https://docs.microsoft.com/en-us/azure/aks/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Azure Pricing Calculator](https://azure.microsoft.com/pricing/calculator/)

---

**Última actualización**: Noviembre 2025
