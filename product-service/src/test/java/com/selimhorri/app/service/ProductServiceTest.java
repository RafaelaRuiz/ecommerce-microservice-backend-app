package com.selimhorri.app.service;

import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;
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
import static org.mockito.Mockito.*;

/**
 * Pruebas Unitarias para ProductService
 * Valida componentes individuales del servicio
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service - Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDto testProductDto;

    @BeforeEach
    void setUp() {
        // Crear producto de prueba
        testProduct = new Product();
        testProduct.setProductId(1);
        testProduct.setProductTitle("Test Product");
        testProduct.setImageUrl("http://example.com/product.jpg");
        testProduct.setSku("TEST-SKU-001");
        testProduct.setPriceUnit(99.99);
        testProduct.setQuantity(50);

        testProductDto = new ProductDto();
        testProductDto.setProductId(1);
        testProductDto.setProductTitle("Test Product");
        testProductDto.setImageUrl("http://example.com/product.jpg");
        testProductDto.setSku("TEST-SKU-001");
        testProductDto.setPriceUnit(99.99);
        testProductDto.setQuantity(50);
    }

    // ============================================
    // PRUEBA UNITARIA 1: Buscar producto por ID existente
    // ============================================
    @Test
    @DisplayName("1. Debe retornar producto cuando ID existe")
    void testFindById_ShouldReturnProduct_WhenIdExists() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));

        // When
        ProductDto result = productService.findById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductTitle()).isEqualTo("Test Product");
        assertThat(result.getSku()).isEqualTo("TEST-SKU-001");
        assertThat(result.getPriceUnit()).isEqualTo(99.99);
        verify(productRepository, times(1)).findById(1);
        
        System.out.println("✅ Test 1 passed: Product found by ID");
    }

    // ============================================
    // PRUEBA UNITARIA 2: Buscar producto por ID inexistente
    // ============================================
    @Test
    @DisplayName("2. Debe lanzar ProductNotFoundException cuando ID no existe")
    void testFindById_ShouldThrowException_WhenIdDoesNotExist() {
        // Given
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.findById(999))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product with id: 999 not found");
        verify(productRepository, times(1)).findById(999);
        
        System.out.println("✅ Test 2 passed: Exception thrown for non-existent ID");
    }

    // ============================================
    // PRUEBA UNITARIA 3: Guardar producto válido
    // ============================================
    @Test
    @DisplayName("3. Debe persistir producto cuando datos son válidos")
    void testSave_ShouldPersistProduct_WhenValidData() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDto result = productService.save(testProductDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1);
        assertThat(result.getProductTitle()).isEqualTo("Test Product");
        verify(productRepository, times(1)).save(any(Product.class));
        
        System.out.println("✅ Test 3 passed: Product saved successfully");
    }

    // ============================================
    // PRUEBA UNITARIA 4: Listar todos los productos
    // ============================================
    @Test
    @DisplayName("4. Debe retornar todos los productos disponibles")
    void testFindAll_ShouldReturnAllProducts() {
        // Given
        Product product1 = new Product();
        product1.setProductId(1);
        product1.setProductTitle("Product 1");
        product1.setSku("SKU-001");
        product1.setPriceUnit(99.99);
        product1.setQuantity(10);

        Product product2 = new Product();
        product2.setProductId(2);
        product2.setProductTitle("Product 2");
        product2.setSku("SKU-002");
        product2.setPriceUnit(149.99);
        product2.setQuantity(20);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        // When
        List<ProductDto> result = productService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductTitle()).isEqualTo("Product 1");
        assertThat(result.get(1).getProductTitle()).isEqualTo("Product 2");
        verify(productRepository, times(1)).findAll();
        
        System.out.println("✅ Test 4 passed: All products retrieved");
    }

    // ============================================
    // PRUEBA UNITARIA 5: Actualizar producto existente
    // ============================================
    @Test
    @DisplayName("5. Debe actualizar producto existente correctamente")
    void testUpdate_ShouldUpdateProduct_WhenValidData() {
        // Given
        testProductDto.setProductTitle("Updated Product");
        testProductDto.setPriceUnit(149.99);
        
        testProduct.setProductTitle("Updated Product");
        testProduct.setPriceUnit(149.99);

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDto result = productService.update(testProductDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductTitle()).isEqualTo("Updated Product");
        assertThat(result.getPriceUnit()).isEqualTo(149.99);
        verify(productRepository, times(1)).save(any(Product.class));
        
        System.out.println("✅ Test 5 passed: Product updated successfully");
    }

    // ============================================
    // PRUEBA UNITARIA 6: Eliminar producto por ID
    // ============================================
    @Test
    @DisplayName("6. Debe eliminar producto cuando ID existe")
    void testDeleteById_ShouldRemoveProduct_WhenIdExists() {
        // Given
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(any(Product.class));

        // When
        productService.deleteById(1);

        // Then
        verify(productRepository, times(1)).findById(1);
        verify(productRepository, times(1)).delete(any(Product.class));
        
        System.out.println("✅ Test 6 passed: Product deleted successfully");
    }

    // ============================================
    // PRUEBA UNITARIA 7: Validar que findAll retorna lista inmutable
    // ============================================
    @Test
    @DisplayName("7. Debe retornar lista inmutable de productos")
    void testFindAll_ShouldReturnImmutableList() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));

        // When
        List<ProductDto> result = productService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThatThrownBy(() -> result.add(new ProductDto()))
            .isInstanceOf(UnsupportedOperationException.class);
        
        System.out.println("✅ Test 7 passed: Immutable list returned");
    }

    // ============================================
    // PRUEBA UNITARIA 8: Validar duplicados removidos en findAll
    // ============================================
    @Test
    @DisplayName("8. Debe remover duplicados al listar productos")
    void testFindAll_ShouldRemoveDuplicates() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, testProduct));

        // When
        List<ProductDto> result = productService.findAll();

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository, times(1)).findAll();
        
        System.out.println("✅ Test 8 passed: Duplicates removed from list");
    }
}
