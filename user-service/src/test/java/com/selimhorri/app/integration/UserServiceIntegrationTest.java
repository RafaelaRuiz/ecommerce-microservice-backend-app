package com.selimhorri.app.integration;

import com.selimhorri.app.dto.CredentialDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas de Integración para User Service
 * Valida la comunicación entre componentes (Controller → Service → Repository)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("User Service - Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static CredentialDto testCredential;

    @BeforeAll
    static void setUpBeforeClass() {
        testCredential = new CredentialDto();
        testCredential.setUsername("integrationuser");
        testCredential.setPassword("integration123");
        testCredential.setIsEnabled(true);
        testCredential.setIsAccountNonExpired(true);
        testCredential.setIsAccountNonLocked(true);
        testCredential.setIsCredentialsNonExpired(true);
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 1: Health Check con Actuator
    // ============================================
    @Test
    @Order(1)
    @DisplayName("1. Health Endpoint debe retornar status UP")
    void testHealthEndpoint_ShouldReturnUp() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/health",
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
        
        System.out.println("✅ INTEGRACIÓN 1: Health check OK - " + response.getBody());
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 2: Crear credencial vía REST API
    // ============================================
    @Test
    @Order(2)
    @DisplayName("2. POST /api/credentials debe crear nueva credencial")
    void testCreateCredential_ShouldReturn201Created() {
        // When
        ResponseEntity<CredentialDto> response = restTemplate.postForEntity(
            "/api/credentials",
            testCredential,
            CredentialDto.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("integrationuser");
        assertThat(response.getBody().getCredentialId()).isNotNull();
        
        // Guardar ID para pruebas posteriores
        testCredential.setCredentialId(response.getBody().getCredentialId());
        
        System.out.println("✅ INTEGRACIÓN 2: Credencial creada con ID: " + testCredential.getCredentialId());
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 3: Obtener credencial por ID
    // ============================================
    @Test
    @Order(3)
    @DisplayName("3. GET /api/credentials/{id} debe retornar credencial existente")
    void testGetCredentialById_ShouldReturn200OK() {
        // When
        ResponseEntity<CredentialDto> response = restTemplate.getForEntity(
            "/api/credentials/" + testCredential.getCredentialId(),
            CredentialDto.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("integrationuser");
        
        System.out.println("✅ INTEGRACIÓN 3: Credencial obtenida: " + response.getBody().getUsername());
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 4: Listar todas las credenciales
    // ============================================
    @Test
    @Order(4)
    @DisplayName("4. GET /api/credentials debe retornar lista de credenciales")
    void testListAllCredentials_ShouldReturnCredentialsList() {
        // When
        ResponseEntity<CredentialDto[]> response = restTemplate.getForEntity(
            "/api/credentials",
            CredentialDto[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
        
        System.out.println("✅ INTEGRACIÓN 4: Total credenciales: " + response.getBody().length);
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 5: Actualizar credencial
    // ============================================
    @Test
    @Order(5)
    @DisplayName("5. PUT /api/credentials/{id} debe actualizar credencial")
    void testUpdateCredential_ShouldReturn200OK() {
        // Given
        testCredential.setUsername("integrationuser_updated");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CredentialDto> requestEntity = new HttpEntity<>(testCredential, headers);

        // When
        ResponseEntity<CredentialDto> response = restTemplate.exchange(
            "/api/credentials",
            HttpMethod.PUT,
            requestEntity,
            CredentialDto.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("integrationuser_updated");
        
        System.out.println("✅ INTEGRACIÓN 5: Credencial actualizada: " + response.getBody().getUsername());
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 6 (BONUS): Verificar métricas Prometheus
    // ============================================
    @Test
    @Order(6)
    @DisplayName("6. Actuator debe exponer métricas de Prometheus")
    void testPrometheusMetrics_ShouldBeExposed() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/prometheus",
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("jvm_memory_used_bytes");
        assertThat(response.getBody()).contains("http_server_requests_seconds");
        
        System.out.println("✅ INTEGRACIÓN 6: Métricas Prometheus disponibles");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 7 (BONUS): Validar info de aplicación
    // ============================================
    @Test
    @Order(7)
    @DisplayName("7. Actuator debe exponer información de la aplicación")
    void testApplicationInfo_ShouldBeExposed() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/actuator/info",
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        System.out.println("✅ INTEGRACIÓN 7: Info de aplicación disponible");
    }

    // ============================================
    // PRUEBA DE INTEGRACIÓN 8 (BONUS): Eliminar credencial
    // ============================================
    @Test
    @Order(8)
    @DisplayName("8. DELETE /api/credentials/{id} debe eliminar credencial")
    void testDeleteCredential_ShouldReturn200OK() {
        // When
        restTemplate.delete("/api/credentials/" + testCredential.getCredentialId());

        // Verificar que ya no existe
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/credentials/" + testCredential.getCredentialId(),
            String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        System.out.println("✅ INTEGRACIÓN 8: Credencial eliminada correctamente");
    }
}
