package com.selimhorri.app.service;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.service.impl.CredentialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Pruebas Unitarias para CredentialService
 * Valida componentes individuales del servicio
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Credential Service - Unit Tests")
class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @InjectMocks
    private CredentialServiceImpl credentialService;

    private Credential testCredential;
    private CredentialDto testCredentialDto;

    @BeforeEach
    void setUp() {
        // Crear credencial de prueba
        testCredential = new Credential();
        testCredential.setCredentialId(1);
        testCredential.setUsername("testuser");
        testCredential.setPassword("password123");
        testCredential.setIsEnabled(true);
        testCredential.setIsAccountNonExpired(true);
        testCredential.setIsAccountNonLocked(true);
        testCredential.setIsCredentialsNonExpired(true);

        // Crear DTO de prueba
        testCredentialDto = new CredentialDto();
        testCredentialDto.setCredentialId(1);
        testCredentialDto.setUsername("testuser");
        testCredentialDto.setPassword("password123");
        testCredentialDto.setIsEnabled(true);
        testCredentialDto.setIsAccountNonExpired(true);
        testCredentialDto.setIsAccountNonLocked(true);
        testCredentialDto.setIsCredentialsNonExpired(true);
    }

    // ============================================
    // PRUEBA UNITARIA 1: Buscar credencial por ID existente
    // ============================================
    @Test
    @DisplayName("1. Debe retornar credencial cuando ID existe")
    void testFindById_ShouldReturnCredential_WhenIdExists() {
        // Given
        when(credentialRepository.findById(1)).thenReturn(Optional.of(testCredential));

        // When
        CredentialDto result = credentialService.findById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCredentialId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getIsEnabled()).isTrue();
        
        verify(credentialRepository, times(1)).findById(1);
    }

    // ============================================
    // PRUEBA UNITARIA 2: Buscar credencial por ID inexistente
    // ============================================
    @Test
    @DisplayName("2. Debe lanzar CredentialNotFoundException cuando ID no existe")
    void testFindById_ShouldThrowException_WhenIdDoesNotExist() {
        // Given
        when(credentialRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> credentialService.findById(999))
            .isInstanceOf(CredentialNotFoundException.class)
            .hasMessageContaining("Credential with id: 999 not found!");
        
        verify(credentialRepository, times(1)).findById(999);
    }

    // ============================================
    // PRUEBA UNITARIA 3: Guardar credencial válida
    // ============================================
    @Test
    @DisplayName("3. Debe persistir credencial cuando datos son válidos")
    void testSave_ShouldPersistCredential_WhenValidData() {
        // Given
        when(credentialRepository.save(any(Credential.class))).thenReturn(testCredential);

        // When
        CredentialDto result = credentialService.save(testCredentialDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCredentialId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("testuser");
        
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    // ============================================
    // PRUEBA UNITARIA 4: Buscar credencial por username
    // ============================================
    @Test
    @DisplayName("4. Debe retornar credencial cuando username existe")
    void testFindByUsername_ShouldReturnCredential_WhenUsernameExists() {
        // Given
        when(credentialRepository.findByUsername("testuser")).thenReturn(Optional.of(testCredential));

        // When
        CredentialDto result = credentialService.findByUsername("testuser");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("password123");
        
        verify(credentialRepository, times(1)).findByUsername("testuser");
    }

    // ============================================
    // PRUEBA UNITARIA 5: Buscar credencial por username inexistente
    // ============================================
    @Test
    @DisplayName("5. Debe lanzar UserObjectNotFoundException cuando username no existe")
    void testFindByUsername_ShouldThrowException_WhenUsernameDoesNotExist() {
        // Given
        String nonExistentUsername = "nonexistent";
        when(credentialRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> credentialService.findByUsername(nonExistentUsername))
            .isInstanceOf(UserObjectNotFoundException.class)
            .hasMessageContaining("Credential with username: nonexistent not found!");
        
        verify(credentialRepository, times(1)).findByUsername(nonExistentUsername);
    }

    // ============================================
    // PRUEBA UNITARIA 6 (BONUS): Listar todas las credenciales
    // ============================================
    @Test
    @DisplayName("6. Debe retornar todas las credenciales disponibles")
    void testFindAll_ShouldReturnAllCredentials() {
        // Given
        Credential cred1 = new Credential();
        cred1.setCredentialId(1);
        cred1.setUsername("user1");

        Credential cred2 = new Credential();
        cred2.setCredentialId(2);
        cred2.setUsername("user2");

        when(credentialRepository.findAll()).thenReturn(Arrays.asList(cred1, cred2));

        // When
        List<CredentialDto> result = credentialService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(CredentialDto::getUsername)
                          .containsExactlyInAnyOrder("user1", "user2");
        
        verify(credentialRepository, times(1)).findAll();
    }

    // ============================================
    // PRUEBA UNITARIA 7 (BONUS): Actualizar credencial
    // ============================================
    @Test
    @DisplayName("7. Debe actualizar credencial existente correctamente")
    void testUpdate_ShouldUpdateCredential_WhenValidData() {
        // Given
        Credential updatedCredential = new Credential();
        updatedCredential.setCredentialId(1);
        updatedCredential.setUsername("updateduser");
        updatedCredential.setPassword("newpassword");

        when(credentialRepository.save(any(Credential.class))).thenReturn(updatedCredential);

        // When
        CredentialDto updatedDto = new CredentialDto();
        updatedDto.setCredentialId(1);
        updatedDto.setUsername("updateduser");
        updatedDto.setPassword("newpassword");
        
        CredentialDto result = credentialService.update(updatedDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("updateduser");
        
        verify(credentialRepository, times(1)).save(any(Credential.class));
    }

    // ============================================
    // PRUEBA UNITARIA 8 (BONUS): Eliminar credencial por ID
    // ============================================
    @Test
    @DisplayName("8. Debe eliminar credencial cuando ID existe")
    void testDeleteById_ShouldRemoveCredential_WhenIdExists() {
        // Given
        doNothing().when(credentialRepository).deleteById(1);

        // When
        credentialService.deleteById(1);

        // Then
        verify(credentialRepository, times(1)).deleteById(1);
    }
}
