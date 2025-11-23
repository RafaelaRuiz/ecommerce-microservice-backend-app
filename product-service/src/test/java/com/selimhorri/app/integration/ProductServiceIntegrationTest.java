package com.selimhorri.app.integration;

import com.selimhorri.app.dto.ProductDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de Integración para Product Service
 * Valida la comunicación entre componentes
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Product Service - Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static ProductDto testProduct;
    private static Integer createdProductId;
    private static final String BASE_PATH = "/api/products";

    @BeforeAll
    static void setUpBeforeClass() {
        testProduct = new ProductDto();
        testProduct.setProductTitle("Integration Test Product");
        testProduct.setImageUrl("http://example.com/product.jpg");
        testProduct.setSku("INT-TEST-001");
        testProduct.setPriceUnit(199.99);
        testProduct.setQuantity(100);
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 1: Health Check
    // ============================================
    @Test
    @Order(1)
    @DisplayName("1. Health Endpoint debe retornar status UP")
    void testHealthEndpoint_ShouldReturnUp() {
        System.out.println("\n🏥 Test 1: Verificando health endpoint...");
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/health",
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
        
        System.out.println("✅ Test 1 passed: Health endpoint is UP");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 2: Crear producto vía REST API
    // ============================================
    @Test
    @Order(2)
    @DisplayName("2. POST /api/products debe crear nuevo producto")
    void testCreateProduct_ShouldReturn201Created() {
        System.out.println("\n➕ Test 2: Creando producto vía POST...");
        
        // When
        ResponseEntity<ProductDto> response = restTemplate.postForEntity(
            BASE_PATH,
            testProduct,
            ProductDto.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductTitle()).isEqualTo("Integration Test Product");
        assertThat(response.getBody().getSku()).isEqualTo("INT-TEST-001");
        
        createdProductId = response.getBody().getProductId();
        System.out.println("   ✓ Producto creado con ID: " + createdProductId);
        System.out.println("✅ Test 2 passed: Product created successfully");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 3: Obtener producto por ID
    // ============================================
    @Test
    @Order(3)
    @DisplayName("3. GET /api/products/{id} debe retornar producto existente")
    void testGetProductById_ShouldReturn200OK() {
        System.out.println("\n🔍 Test 3: Obteniendo producto por ID...");
        
        // When
        ResponseEntity<ProductDto> response = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            ProductDto.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo(createdProductId);
        assertThat(response.getBody().getSku()).isEqualTo("INT-TEST-001");
        
        System.out.println("✅ Test 3 passed: Product retrieved by ID");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 4: Listar todos los productos
    // ============================================
    @Test
    @Order(4)
    @DisplayName("4. GET /api/products debe retornar lista de productos")
    void testListAllProducts_ShouldReturnProductsList() {
        System.out.println("\n📋 Test 4: Listando todos los productos...");
        
        // When
        ResponseEntity<ProductDto[]> response = restTemplate.getForEntity(
            BASE_PATH,
            ProductDto[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        
        System.out.println("   ✓ Total de productos: " + response.getBody().length);
        System.out.println("✅ Test 4 passed: Products list retrieved");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 5: Actualizar producto
    // ============================================
    @Test
    @Order(5)
    @DisplayName("5. PUT /api/products debe actualizar producto")
    void testUpdateProduct_ShouldReturn200OK() {
        System.out.println("\n✏️ Test 5: Actualizando producto...");
        
        // Given
        ProductDto updatedProduct = new ProductDto();
        updatedProduct.setProductId(createdProductId);
        updatedProduct.setProductTitle("Updated Product");
        updatedProduct.setSku("INT-TEST-001");
        updatedProduct.setPriceUnit(249.99);
        updatedProduct.setQuantity(150);
        updatedProduct.setImageUrl("http://example.com/updated.jpg");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductDto> requestEntity = new HttpEntity<>(updatedProduct, headers);

        // When
        ResponseEntity<ProductDto> response = restTemplate.exchange(
            BASE_PATH,
            HttpMethod.PUT,
            requestEntity,
            ProductDto.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductTitle()).isEqualTo("Updated Product");
        assertThat(response.getBody().getPriceUnit()).isEqualTo(249.99);
        
        System.out.println("✅ Test 5 passed: Product updated successfully");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 6: Métricas Prometheus
    // ============================================
    @Test
    @Order(6)
    @DisplayName("6. Actuator debe exponer métricas de Prometheus")
    void testPrometheusMetrics_ShouldBeExposed() {
        System.out.println("\n📊 Test 6: Verificando métricas de Prometheus...");
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/prometheus",
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("jvm_memory_used_bytes");
        assertThat(response.getBody()).contains("http_server_requests");
        
        System.out.println("✅ Test 6 passed: Prometheus metrics exposed");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 7: Información de la aplicación
    // ============================================
    @Test
    @Order(7)
    @DisplayName("7. Actuator debe exponer información de la aplicación")
    void testApplicationInfo_ShouldBeExposed() {
        System.out.println("\n📄 Test 7: Verificando información de la aplicación...");
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/info",
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        System.out.println("✅ Test 7 passed: Application info exposed");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 8: Eliminar producto
    // ============================================
    @Test
    @Order(8)
    @DisplayName("8. DELETE /api/products/{id} debe eliminar producto")
    void testDeleteProduct_ShouldReturn200OK() {
        System.out.println("\n🗑️ Test 8: Eliminando producto...");
        
        // When
        restTemplate.delete(BASE_PATH + "/" + createdProductId);

        // Then - Verificar que ya no existe
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            String.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        System.out.println("✅ Test 8 passed: Product deleted successfully");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("\n✅ Todas las pruebas de integración completadas para Product Service");
    }
}
