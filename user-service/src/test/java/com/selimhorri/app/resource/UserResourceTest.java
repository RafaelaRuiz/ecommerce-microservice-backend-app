package com.selimhorri.app.resource;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.service.UserService;

@WebMvcTest(UserResource.class)
@ActiveProfiles("test")
class UserResourceTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Test
    void getById_ok() throws Exception {
        when(userService.findById(1)).thenReturn(UserDto.builder().userId(1).build());
        mockMvc.perform(get("/api/users/1")).andExpect(status().isOk());
    }
}