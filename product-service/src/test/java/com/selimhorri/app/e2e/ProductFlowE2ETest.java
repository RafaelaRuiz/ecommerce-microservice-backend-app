package com.selimhorri.app.e2e;

import com.selimhorri.app.dto.ProductDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas End-to-End para Product Service
 * Valida flujos completos de gestión de productos
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Product Service - End-to-End Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductFlowE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static Integer createdProductId;
    private static final String BASE_PATH = "/api/products";

    // ============================================
    // E2E FLUJO 1: Registro completo de nuevo producto
    // ============================================
    @Test
    @Order(1)
    @DisplayName("E2E 1: Flujo completo de registro de nuevo producto")
    void testE2E_ProductRegistration_CompleteFlow() {
        System.out.println("\n🚀 E2E 1: Iniciando flujo de registro de producto");

        // PASO 1: Crear producto
        System.out.println("   📝 PASO 1: Crear producto...");
        ProductDto newProduct = new ProductDto();
        newProduct.setProductTitle("E2E Test Product");
        newProduct.setImageUrl("http://example.com/e2e-product.jpg");
        newProduct.setSku("E2E-001");
        newProduct.setPriceUnit(299.99);
        newProduct.setQuantity(50);

        ResponseEntity<ProductDto> createResponse = restTemplate.postForEntity(
            BASE_PATH,
            newProduct,
            ProductDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        createdProductId = createResponse.getBody().getProductId();
        
        System.out.println("      ✓ Producto creado con ID: " + createdProductId);

        // PASO 2: Verificar que el producto existe
        System.out.println("   🔍 PASO 2: Verificar que el producto existe...");
        ResponseEntity<ProductDto> getResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            ProductDto.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getSku()).isEqualTo("E2E-001");
        assertThat(getResponse.getBody().getProductTitle()).isEqualTo("E2E Test Product");
        
        System.out.println("      ✓ Producto verificado exitosamente");

        // PASO 3: Verificar que aparece en el listado
        System.out.println("   📋 PASO 3: Verificar que aparece en el listado...");
        ResponseEntity<ProductDto[]> listResponse = restTemplate.getForEntity(
            BASE_PATH,
            ProductDto[].class
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
        
        boolean found = false;
        for (ProductDto p : listResponse.getBody()) {
            if (p.getProductId().equals(createdProductId)) {
                found = true;
                break;
            }
        }
        assertThat(found).isTrue();
        
        System.out.println("      ✓ Producto encontrado en el listado");
        System.out.println("✅ E2E 1 COMPLETADO: Registro de producto exitoso\n");
    }

    // ============================================
    // E2E FLUJO 2: Actualización de producto
    // ============================================
    @Test
    @Order(2)
    @DisplayName("E2E 2: Flujo completo de actualización de producto")
    void testE2E_ProductUpdate_CompleteFlow() {
        System.out.println("📝 E2E 2: Iniciando flujo de actualización de producto");

        // PASO 1: Obtener producto actual
        System.out.println("   📖 PASO 1: Obtener producto actual...");
        ResponseEntity<ProductDto> getResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            ProductDto.class
        );

        ProductDto existingProduct = getResponse.getBody();
        assertThat(existingProduct).isNotNull();
        
        System.out.println("      ✓ Producto obtenido: " + existingProduct.getProductTitle());

        // PASO 2: Actualizar producto
        System.out.println("   ✏️ PASO 2: Actualizar producto...");
        existingProduct.setProductTitle("E2E Updated Product");
        existingProduct.setPriceUnit(349.99);
        existingProduct.setQuantity(75);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductDto> requestEntity = new HttpEntity<>(existingProduct, headers);

        ResponseEntity<ProductDto> updateResponse = restTemplate.exchange(
            BASE_PATH,
            HttpMethod.PUT,
            requestEntity,
            ProductDto.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getProductTitle()).isEqualTo("E2E Updated Product");
        assertThat(updateResponse.getBody().getPriceUnit()).isEqualTo(349.99);

        System.out.println("      ✓ Producto actualizado exitosamente");

        // PASO 3: Verificar persistencia de los cambios
        System.out.println("   🔍 PASO 3: Verificar persistencia de cambios...");
        ResponseEntity<ProductDto> verifyResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            ProductDto.class
        );

        assertThat(verifyResponse.getBody().getProductTitle()).isEqualTo("E2E Updated Product");
        assertThat(verifyResponse.getBody().getPriceUnit()).isEqualTo(349.99);
        assertThat(verifyResponse.getBody().getQuantity()).isEqualTo(75);

        System.out.println("      ✓ Cambios persistidos correctamente");
        System.out.println("✅ E2E 2 COMPLETADO: Actualización de producto exitosa\n");
    }

    // ============================================
    // E2E FLUJO 3: Búsqueda y validación de producto
    // ============================================
    @Test
    @Order(3)
    @DisplayName("E2E 3: Flujo completo de búsqueda y validación")
    void testE2E_ProductSearchAndValidation_CompleteFlow() {
        System.out.println("🔍 E2E 3: Iniciando flujo de búsqueda y validación");

        // PASO 1: Buscar por ID
        System.out.println("   🔎 PASO 1: Buscar producto por ID...");
        ResponseEntity<ProductDto> response = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            ProductDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        
        System.out.println("      ✓ Producto encontrado");

        // PASO 2: Validar todos los campos
        System.out.println("   ✓ PASO 2: Validar campos del producto...");
        ProductDto product = response.getBody();
        assertThat(product.getProductTitle()).isEqualTo("E2E Updated Product");
        assertThat(product.getSku()).isEqualTo("E2E-001");
        assertThat(product.getPriceUnit()).isEqualTo(349.99);
        assertThat(product.getQuantity()).isEqualTo(75);

        System.out.println("      ✓ Todos los campos validados correctamente");

        // PASO 3: Intentar buscar producto inexistente
        System.out.println("   🔍 PASO 3: Validar manejo de producto inexistente...");
        ResponseEntity<String> notFoundResponse = restTemplate.getForEntity(
            BASE_PATH + "/999999",
            String.class
        );

        assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        System.out.println("      ✓ Manejo correcto de producto no encontrado");
        System.out.println("✅ E2E 3 COMPLETADO: Búsqueda y validación exitosa\n");
    }

    // ============================================
    // E2E FLUJO 4: Gestión de inventario
    // ============================================
    @Test
    @Order(4)
    @DisplayName("E2E 4: Flujo completo de gestión de inventario")
    void testE2E_InventoryManagement_CompleteFlow() {
        System.out.println("📦 E2E 4: Iniciando flujo de gestión de inventario");

        // PASO 1: Verificar stock actual
        System.out.println("   📊 PASO 1: Verificar stock actual...");
        ResponseEntity<ProductDto> getResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            ProductDto.class
        );

        ProductDto product = getResponse.getBody();
        int initialStock = product.getQuantity();
        
        System.out.println("      ℹ Stock inicial: " + initialStock);

        // PASO 2: Simular venta (reducir stock)
        System.out.println("   💰 PASO 2: Simular venta (reducir stock en 10 unidades)...");
        product.setQuantity(initialStock - 10);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductDto> requestEntity = new HttpEntity<>(product, headers);

        ResponseEntity<ProductDto> updateResponse = restTemplate.exchange(
            BASE_PATH,
            HttpMethod.PUT,
            requestEntity,
            ProductDto.class
        );

        assertThat(updateResponse.getBody().getQuantity()).isEqualTo(initialStock - 10);

        System.out.println("      ✓ Stock actualizado: " + (initialStock - 10));

        // PASO 3: Verificar el nuevo stock
        System.out.println("   🔍 PASO 3: Verificar nuevo stock...");
        ResponseEntity<ProductDto> verifyResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            ProductDto.class
        );

        assertThat(verifyResponse.getBody().getQuantity()).isEqualTo(initialStock - 10);

        System.out.println("      ✓ Stock verificado correctamente");
        System.out.println("✅ E2E 4 COMPLETADO: Gestión de inventario exitosa\n");
    }

    // ============================================
    // E2E FLUJO 5: Listado y búsqueda masiva
    // ============================================
    @Test
    @Order(5)
    @DisplayName("E2E 5: Flujo completo de listado de productos")
    void testE2E_ProductListing_CompleteFlow() {
        System.out.println("📋 E2E 5: Iniciando flujo de listado de productos");

        // PASO 1: Contar productos iniciales
        System.out.println("   📊 PASO 1: Contar productos iniciales...");
        ResponseEntity<ProductDto[]> initialResponse = restTemplate.getForEntity(
            BASE_PATH,
            ProductDto[].class
        );

        int initialCount = initialResponse.getBody().length;
        System.out.println("      ℹ Total de productos inicial: " + initialCount);

        // PASO 2: Crear productos adicionales
        System.out.println("   ➕ PASO 2: Crear 3 productos adicionales...");
        for (int i = 1; i <= 3; i++) {
            ProductDto newProduct = new ProductDto();
            newProduct.setProductTitle("Bulk Product " + i);
            newProduct.setSku("BULK-00" + i);
            newProduct.setPriceUnit(99.99 * i);
            newProduct.setQuantity(10 * i);
            newProduct.setImageUrl("http://example.com/bulk" + i + ".jpg");

            ResponseEntity<ProductDto> createResponse = restTemplate.postForEntity(
                BASE_PATH,
                newProduct,
                ProductDto.class
            );

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            System.out.println("      ✓ Producto " + i + " creado");
        }

        // PASO 3: Verificar incremento en el contador
        System.out.println("   📊 PASO 3: Verificar incremento en el contador...");
        ResponseEntity<ProductDto[]> finalResponse = restTemplate.getForEntity(
            BASE_PATH,
            ProductDto[].class
        );

        int finalCount = finalResponse.getBody().length;
        assertThat(finalCount).isEqualTo(initialCount + 3);

        System.out.println("      ℹ Total de productos final: " + finalCount);
        System.out.println("      ✓ Incremento correcto: +" + (finalCount - initialCount) + " productos");
        System.out.println("✅ E2E 5 COMPLETADO: Listado de productos exitoso\n");
    }

    // ============================================
    // E2E FLUJO 6: Eliminación y limpieza
    // ============================================
    @Test
    @Order(6)
    @DisplayName("E2E 6: Flujo completo de eliminación de producto")
    void testE2E_ProductDeletion_CompleteFlow() {
        System.out.println("🗑️ E2E 6: Iniciando flujo de eliminación de producto");

        // PASO 1: Verificar que el producto existe
        System.out.println("   🔍 PASO 1: Verificar que el producto existe...");
        ResponseEntity<ProductDto> getResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            ProductDto.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("      ✓ Producto existe");

        // PASO 2: Eliminar producto
        System.out.println("   🗑️ PASO 2: Eliminar producto...");
        restTemplate.delete(BASE_PATH + "/" + createdProductId);
        System.out.println("      ✓ Solicitud de eliminación enviada");

        // PASO 3: Verificar que ya no existe
        System.out.println("   🔍 PASO 3: Verificar que ya no existe...");
        ResponseEntity<String> notFoundResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdProductId,
            String.class
        );

        assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println("      ✓ Producto eliminado correctamente (404)");

        // PASO 4: Verificar que no aparece en el listado
        System.out.println("   📋 PASO 4: Verificar que no aparece en el listado...");
        ResponseEntity<ProductDto[]> listResponse = restTemplate.getForEntity(
            BASE_PATH,
            ProductDto[].class
        );

        boolean found = false;
        for (ProductDto p : listResponse.getBody()) {
            if (p.getProductId().equals(createdProductId)) {
                found = true;
                break;
            }
        }
        assertThat(found).isFalse();

        System.out.println("      ✓ Producto no aparece en el listado");
        System.out.println("✅ E2E 6 COMPLETADO: Eliminación de producto exitosa\n");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ TODAS LAS PRUEBAS E2E COMPLETADAS PARA PRODUCT SERVICE");
        System.out.println("   Total de flujos validados: 6");
        System.out.println("   - Registro de producto");
        System.out.println("   - Actualización de producto");
        System.out.println("   - Búsqueda y validación");
        System.out.println("   - Gestión de inventario");
        System.out.println("   - Listado de productos");
        System.out.println("   - Eliminación de producto");
        System.out.println("=".repeat(80) + "\n");
    }
}
