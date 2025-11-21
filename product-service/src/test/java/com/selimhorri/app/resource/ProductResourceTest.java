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

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.service.ProductService;

@WebMvcTest(ProductResource.class)
@ActiveProfiles("test")
class ProductResourceTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductService productService;

    @Test
    void getById_ok() throws Exception {
        when(productService.findById(1)).thenReturn(ProductDto.builder().productId(1).build());
        mockMvc.perform(get("/api/products/1")).andExpect(status().isOk());
    }
}