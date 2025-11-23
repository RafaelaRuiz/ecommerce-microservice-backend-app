package com.selimhorri.app.e2e;

import com.selimhorri.app.dto.CredentialDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Pruebas End-to-End para User Service
 * Valida flujos completos de usuario desde inicio hasta fin
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("User Service - End-to-End Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserFlowE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static Integer createdCredentialId;
    private static final String BASE_PATH = "/api/credentials";

    // ============================================
    // E2E FLUJO 1: Registro completo de nuevo usuario
    // ============================================
    @Test
    @Order(1)
    @DisplayName("E2E 1: Flujo completo de registro de nuevo usuario")
    void testE2E_UserRegistration_CompleteFlow() {
        System.out.println("\n🚀 E2E 1: Iniciando flujo de registro de usuario");

        // PASO 1: Crear credencial
        CredentialDto newUser = new CredentialDto();
        newUser.setUsername("e2e_testuser");
        newUser.setPassword("SecureP@ss123");
        newUser.setIsEnabled(true);
        newUser.setIsAccountNonExpired(true);
        newUser.setIsAccountNonLocked(true);
        newUser.setIsCredentialsNonExpired(true);

        ResponseEntity<CredentialDto> createResponse = restTemplate.postForEntity(
            BASE_PATH,
            newUser,
            CredentialDto.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        createdCredentialId = createResponse.getBody().getCredentialId();
        
        System.out.println("   ✓ Usuario creado con ID: " + createdCredentialId);

        // PASO 2: Verificar que el usuario existe
        ResponseEntity<CredentialDto> getResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdCredentialId,
            CredentialDto.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getUsername()).isEqualTo("e2e_testuser");
        
        System.out.println("   ✓ Usuario verificado: " + getResponse.getBody().getUsername());

        // PASO 3: Verificar que aparece en la lista
        ResponseEntity<CredentialDto[]> listResponse = restTemplate.getForEntity(
            BASE_PATH,
            CredentialDto[].class
        );

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
        
        boolean userExistsInList = Arrays.stream(listResponse.getBody())
            .anyMatch(cred -> cred.getCredentialId().equals(createdCredentialId));
        assertThat(userExistsInList).isTrue();
        
        System.out.println("   ✓ Usuario aparece en listado completo");
        System.out.println("✅ E2E 1: Flujo de registro completado exitosamente\n");
    }

    // ============================================
    // E2E FLUJO 2: Actualización de perfil de usuario
    // ============================================
    @Test
    @Order(2)
    @DisplayName("E2E 2: Flujo completo de actualización de perfil")
    void testE2E_UserProfileUpdate_CompleteFlow() {
        System.out.println("\n🚀 E2E 2: Iniciando flujo de actualización de perfil");

        // PASO 1: Obtener usuario actual
        ResponseEntity<CredentialDto> getResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdCredentialId,
            CredentialDto.class
        );

        CredentialDto currentUser = getResponse.getBody();
        assertThat(currentUser).isNotNull();
        
        System.out.println("   ✓ Usuario actual obtenido: " + currentUser.getUsername());

        // PASO 2: Modificar datos del usuario
        currentUser.setUsername("e2e_testuser_updated");
        currentUser.setPassword("NewSecureP@ss456");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CredentialDto> requestEntity = new HttpEntity<>(currentUser, headers);

        ResponseEntity<CredentialDto> updateResponse = restTemplate.exchange(
            BASE_PATH,
            HttpMethod.PUT,
            requestEntity,
            CredentialDto.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getUsername()).isEqualTo("e2e_testuser_updated");
        
        System.out.println("   ✓ Usuario actualizado: " + updateResponse.getBody().getUsername());

        // PASO 3: Verificar que los cambios persisten
        ResponseEntity<CredentialDto> verifyResponse = restTemplate.getForEntity(
            BASE_PATH + "/" + createdCredentialId,
            CredentialDto.class
        );

        assertThat(verifyResponse.getBody().getUsername()).isEqualTo("e2e_testuser_updated");
        
        System.out.println("   ✓ Cambios verificados y persistidos");
        System.out.println("✅ E2E 2: Flujo de actualización completado exitosamente\n");
    }

    // ============================================
    // E2E FLUJO 3: Búsqueda y validación de usuario
    // ============================================
    @Test
    @Order(3)
    @DisplayName("E2E 3: Flujo completo de búsqueda y validación")
    void testE2E_UserSearchAndValidation_CompleteFlow() {
        System.out.println("\n🚀 E2E 3: Iniciando flujo de búsqueda y validación");

        // PASO 1: Buscar usuario por ID
        ResponseEntity<CredentialDto> searchById = restTemplate.getForEntity(
            BASE_PATH + "/" + createdCredentialId,
            CredentialDto.class
        );

        assertThat(searchById.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        System.out.println("   ✓ Usuario encontrado por ID: " + searchById.getBody().getCredentialId());

        // PASO 2: Validar atributos de seguridad
        CredentialDto foundUser = searchById.getBody();
        assertThat(foundUser.getIsEnabled()).isTrue();
        assertThat(foundUser.getIsAccountNonExpired()).isTrue();
        assertThat(foundUser.getIsAccountNonLocked()).isTrue();
        assertThat(foundUser.getIsCredentialsNonExpired()).isTrue();
        
        System.out.println("   ✓ Atributos de seguridad validados");

        // PASO 3: Intentar buscar usuario inexistente
        ResponseEntity<String> searchNonExistent = restTemplate.getForEntity(
            BASE_PATH + "/99999",
            String.class
        );

        assertThat(searchNonExistent.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        System.out.println("   ✓ Manejo correcto de usuario inexistente (404)");
        System.out.println("✅ E2E 3: Flujo de búsqueda completado exitosamente\n");
    }

    // ============================================
    // E2E FLUJO 4: Gestión de múltiples usuarios
    // ============================================
    @Test
    @Order(4)
    @DisplayName("E2E 4: Flujo completo de gestión múltiple de usuarios")
    void testE2E_MultipleUsersManagement_CompleteFlow() {
        System.out.println("\n🚀 E2E 4: Iniciando flujo de gestión múltiple");

        // PASO 1: Obtener conteo inicial
        ResponseEntity<CredentialDto[]> initialList = restTemplate.getForEntity(
            BASE_PATH,
            CredentialDto[].class
        );

        int initialCount = initialList.getBody().length;
        System.out.println("   ✓ Conteo inicial: " + initialCount + " usuarios");

        // PASO 2: Crear usuarios adicionales
        for (int i = 1; i <= 3; i++) {
            CredentialDto newUser = new CredentialDto();
            newUser.setUsername("e2e_bulk_user_" + i);
            newUser.setPassword("Pass" + i + "@123");
            newUser.setIsEnabled(true);
            newUser.setIsAccountNonExpired(true);
            newUser.setIsAccountNonLocked(true);
            newUser.setIsCredentialsNonExpired(true);

            ResponseEntity<CredentialDto> createResponse = restTemplate.postForEntity(
                BASE_PATH,
                newUser,
                CredentialDto.class
            );

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            System.out.println("   ✓ Usuario " + i + " creado: " + newUser.getUsername());
        }

        // PASO 3: Verificar incremento en la lista
        ResponseEntity<CredentialDto[]> finalList = restTemplate.getForEntity(
            BASE_PATH,
            CredentialDto[].class
        );

        int finalCount = finalList.getBody().length;
        assertThat(finalCount).isGreaterThanOrEqualTo(initialCount + 3);
        
        System.out.println("   ✓ Conteo final: " + finalCount + " usuarios");
        System.out.println("✅ E2E 4: Flujo de gestión múltiple completado exitosamente\n");
    }

    // ============================================
    // E2E FLUJO 5: Eliminación y limpieza de usuario
    // ============================================
    @Test
    @Order(5)
    @DisplayName("E2E 5: Flujo completo de eliminación de usuario")
    void testE2E_UserDeletion_CompleteFlow() {
        System.out.println("\n🚀 E2E 5: Iniciando flujo de eliminación");

        // PASO 1: Verificar que el usuario existe antes de eliminar
        ResponseEntity<CredentialDto> preDeleteCheck = restTemplate.getForEntity(
            BASE_PATH + "/" + createdCredentialId,
            CredentialDto.class
        );

        assertThat(preDeleteCheck.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("   ✓ Usuario confirmado antes de eliminación");

        // PASO 2: Eliminar usuario
        restTemplate.delete(BASE_PATH + "/" + createdCredentialId);
        System.out.println("   ✓ Solicitud de eliminación enviada");

        // PASO 3: Verificar que el usuario fue eliminado
        ResponseEntity<String> postDeleteCheck = restTemplate.getForEntity(
            BASE_PATH + "/" + createdCredentialId,
            String.class
        );

        assertThat(postDeleteCheck.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        System.out.println("   ✓ Usuario eliminado correctamente (404)");

        // PASO 4: Verificar que no aparece en el listado
        ResponseEntity<CredentialDto[]> finalList = restTemplate.getForEntity(
            BASE_PATH,
            CredentialDto[].class
        );

        boolean userStillExists = Arrays.stream(finalList.getBody())
            .anyMatch(cred -> cred.getCredentialId().equals(createdCredentialId));
        assertThat(userStillExists).isFalse();
        
        System.out.println("   ✓ Usuario no aparece en listado");
        System.out.println("✅ E2E 5: Flujo de eliminación completado exitosamente\n");
    }

    // ============================================
    // E2E FLUJO 6 (BONUS): Validación de salud del sistema
    // ============================================
    @Test
    @Order(6)
    @DisplayName("E2E 6: Validación completa de salud del sistema")
    void testE2E_SystemHealthValidation_CompleteFlow() {
        System.out.println("\n🚀 E2E 6: Iniciando validación de salud del sistema");

        // PASO 1: Health check general
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
            "/actuator/health",
            String.class
        );

        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(healthResponse.getBody()).contains("\"status\":\"UP\"");
        System.out.println("   ✓ Health check: UP");

        // PASO 2: Verificar endpoints disponibles
        ResponseEntity<String> endpointsResponse = restTemplate.getForEntity(
            "/actuator",
            String.class
        );

        assertThat(endpointsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println("   ✓ Actuator endpoints accesibles");

        // PASO 3: Validar métricas
        ResponseEntity<String> metricsResponse = restTemplate.getForEntity(
            "/actuator/metrics",
            String.class
        );

        assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metricsResponse.getBody()).contains("jvm.memory.used");
        System.out.println("   ✓ Métricas disponibles");

        System.out.println("✅ E2E 6: Validación de salud completada exitosamente\n");
    }
}
