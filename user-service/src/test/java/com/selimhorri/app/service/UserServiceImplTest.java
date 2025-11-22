package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.ActiveProfiles;

import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles("test")
class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    MeterRegistry meterRegistry;

    @Mock
    Counter counter;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setup() {
        when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    @Test
    void findById_returnsDto() {
        com.selimhorri.app.domain.Credential cred = com.selimhorri.app.domain.Credential.builder()
            .credentialId(10)
            .username("u")
            .password("p")
            .roleBasedAuthority(com.selimhorri.app.domain.RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
        User user = User.builder().userId(1).firstName("a").lastName("b").credential(cred).build();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        UserDto dto = userService.findById(1);
        assertEquals(1, dto.getUserId());
    }

    @Test
    void save_mapsAndPersists() {
        com.selimhorri.app.dto.CredentialDto credDto = com.selimhorri.app.dto.CredentialDto.builder()
            .credentialId(10)
            .username("u")
            .password("p")
            .roleBasedAuthority(com.selimhorri.app.domain.RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
        UserDto in = UserDto.builder().firstName("a").lastName("b").credentialDto(credDto).build();
        com.selimhorri.app.domain.Credential cred = com.selimhorri.app.domain.Credential.builder()
            .credentialId(10)
            .username("u")
            .password("p")
            .roleBasedAuthority(com.selimhorri.app.domain.RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
        User persisted = User.builder().userId(2).firstName("a").lastName("b").credential(cred).build();
        when(userRepository.save(any(User.class))).thenReturn(persisted);
        UserDto out = userService.save(in);
        assertEquals(2, out.getUserId());
    }
}