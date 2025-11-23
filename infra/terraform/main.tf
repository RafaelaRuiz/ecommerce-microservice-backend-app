# Configure the Azure Provider
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
  required_version = ">= 1.0"
}

provider "azurerm" {
  features {}
}

# Resource Group
resource "azurerm_resource_group" "ecommerce_rg" {
  name     = var.resource_group_name
  location = var.location
  tags = {
    environment = var.environment
    project     = "ecommerce-microservices"
  }
}

# Azure Kubernetes Service (AKS)
resource "azurerm_kubernetes_cluster" "ecommerce_aks" {
  name                = var.aks_cluster_name
  location            = azurerm_resource_group.ecommerce_rg.location
  resource_group_name = azurerm_resource_group.ecommerce_rg.name
  dns_prefix          = "${var.aks_cluster_name}-dns"

  default_node_pool {
    name       = "default"
    node_count = var.node_count
    vm_size    = var.vm_size
    
    # Enable auto-scaling (optional)
    enable_auto_scaling = true
    min_count          = var.min_node_count
    max_count          = var.max_node_count
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin    = "azure"
    load_balancer_sku = "standard"
    network_policy    = "azure"
  }

  tags = {
    environment = var.environment
    project     = "ecommerce-microservices"
  }
}

# Container Registry (ACR) - Optional, si decides usar ACR en vez de Docker Hub
resource "azurerm_container_registry" "ecommerce_acr" {
  count               = var.create_acr ? 1 : 0
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.ecommerce_rg.name
  location            = azurerm_resource_group.ecommerce_rg.location
  sku                 = "Basic"
  admin_enabled       = true

  tags = {
    environment = var.environment
    project     = "ecommerce-microservices"
  }
}

# Attach ACR to AKS (if ACR is created)
resource "azurerm_role_assignment" "aks_acr_pull" {
  count                = var.create_acr ? 1 : 0
  principal_id         = azurerm_kubernetes_cluster.ecommerce_aks.kubelet_identity[0].object_id
  role_definition_name = "AcrPull"
  scope                = azurerm_container_registry.ecommerce_acr[0].id
}
