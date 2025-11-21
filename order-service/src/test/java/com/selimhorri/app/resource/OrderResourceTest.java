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

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.service.OrderService;

@WebMvcTest(OrderResource.class)
@ActiveProfiles("test")
class OrderResourceTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderService orderService;

    @Test
    void getById_ok() throws Exception {
        when(orderService.findById(1)).thenReturn(OrderDto.builder().orderId(1).build());
        mockMvc.perform(get("/api/orders/1")).andExpect(status().isOk());
    }
}