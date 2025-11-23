# Terraform Variables Configuration
# Customize these values according to your needs

resource_group_name = "ecommerce-microservices-rg"
location            = "eastus"  # Cambiar según tu región preferida
environment         = "dev"

aks_cluster_name = "ecommerce-aks-cluster"
node_count       = 2
min_node_count   = 1
max_node_count   = 5
vm_size          = "Standard_D2s_v3"

# Container Registry
create_acr = false  # Usaremos Docker Hub, cambiar a true si quieres usar ACR
acr_name   = "ecommerceacr"  # Debe ser único globalmente si create_acr = true
