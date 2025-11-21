package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ProductServiceImplTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductServiceImpl productService;

    @Test
    void findById_returnsDto() {
        com.selimhorri.app.domain.Category category = com.selimhorri.app.domain.Category.builder()
            .categoryId(7)
            .categoryTitle("c")
            .imageUrl("i")
            .build();
        Product product = Product.builder().productId(1).productTitle("p").category(category).build();
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        ProductDto dto = productService.findById(1);
        assertEquals(1, dto.getProductId());
    }

    @Test
    void save_mapsAndPersists() {
        com.selimhorri.app.dto.CategoryDto categoryDto = com.selimhorri.app.dto.CategoryDto.builder()
            .categoryId(7)
            .categoryTitle("c")
            .imageUrl("i")
            .build();
        ProductDto in = ProductDto.builder().productTitle("p").categoryDto(categoryDto).build();
        com.selimhorri.app.domain.Category category = com.selimhorri.app.domain.Category.builder()
            .categoryId(7)
            .categoryTitle("c")
            .imageUrl("i")
            .build();
        Product persisted = Product.builder().productId(2).productTitle("p").category(category).build();
        when(productRepository.save(any(Product.class))).thenReturn(persisted);
        ProductDto out = productService.save(in);
        assertEquals(2, out.getProductId());
    }
}