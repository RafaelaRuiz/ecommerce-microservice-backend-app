output "resource_group_name" {
  description = "Name of the resource group"
  value       = azurerm_resource_group.ecommerce_rg.name
}

output "aks_cluster_name" {
  description = "Name of the AKS cluster"
  value       = azurerm_kubernetes_cluster.ecommerce_aks.name
}

output "aks_cluster_id" {
  description = "ID of the AKS cluster"
  value       = azurerm_kubernetes_cluster.ecommerce_aks.id
}

output "aks_kubeconfig" {
  description = "Kubeconfig for connecting to the AKS cluster"
  value       = azurerm_kubernetes_cluster.ecommerce_aks.kube_config_raw
  sensitive   = true
}

output "aks_fqdn" {
  description = "FQDN of the AKS cluster"
  value       = azurerm_kubernetes_cluster.ecommerce_aks.fqdn
}

output "acr_login_server" {
  description = "Login server URL for the Azure Container Registry"
  value       = var.create_acr ? azurerm_container_registry.ecommerce_acr[0].login_server : "N/A - Using Docker Hub"
}

output "acr_admin_username" {
  description = "Admin username for the Azure Container Registry"
  value       = var.create_acr ? azurerm_container_registry.ecommerce_acr[0].admin_username : "N/A"
  sensitive   = true
}

output "acr_admin_password" {
  description = "Admin password for the Azure Container Registry"
  value       = var.create_acr ? azurerm_container_registry.ecommerce_acr[0].admin_password : "N/A"
  sensitive   = true
}
