package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.selimhorri.app.dto.ProductDto;
import io.micrometer.core.instrument.MeterRegistry;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.helper.ProductMappingHelper;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
	
    private final ProductRepository productRepository;
    private final MeterRegistry meterRegistry;
	
	/**
	 * Listar productos con Retry para timeouts de DB
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 2,
		backoff = @Backoff(delay = 500)
	)
	public List<ProductDto> findAll() {
		log.info("📦 [Attempt] Finding all products");
		return this.productRepository.findAll()
				.stream()
					.map(ProductMappingHelper::map)
					.distinct()
					.collect(Collectors.toUnmodifiableList());
	}
	
	/**
	 * Buscar producto con Retry para timeouts de DB
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 2,
		backoff = @Backoff(delay = 500)
	)
	public ProductDto findById(final Integer productId) {
		log.info("📦 [Attempt] Finding product: {}", productId);
		return this.productRepository.findById(productId)
				.map(ProductMappingHelper::map)
				.orElseThrow(() -> new ProductNotFoundException(String.format("Product with id: %d not found", productId)));
	}
	
	@Recover
	public ProductDto recoverFindById(DataAccessException ex, Integer productId) {
		log.error("❌ [Recover] Failed to find product after retries: {}", productId, ex);
		throw new ProductNotFoundException("Database temporarily unavailable");
	}
	
	/**
	 * Guardar producto con Retry para deadlocks
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2.0)
	)
	public ProductDto save(final ProductDto productDto) {
		log.info("💾 [Attempt] Saving product: {}", productDto.getProductTitle());
		return ProductMappingHelper.map(this.productRepository
				.save(ProductMappingHelper.map(productDto)));
	}
	
	@Recover
	public ProductDto recoverSave(DataAccessException ex, ProductDto productDto) {
		log.error("❌ [Recover] Failed to save product after retries: {}", 
			productDto.getProductTitle(), ex);
		throw new RuntimeException("Failed to save product: database issue");
	}
	
	/**
	 * Actualizar producto con Retry
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2.0)
	)
	public ProductDto update(final ProductDto productDto) {
		log.info("🔄 [Attempt] Updating product: {}", productDto.getProductId());
		return ProductMappingHelper.map(this.productRepository
				.save(ProductMappingHelper.map(productDto)));
	}
	
	@Recover
	public ProductDto recoverUpdate(DataAccessException ex, ProductDto productDto) {
		log.error("❌ [Recover] Failed to update product after retries: {}", 
			productDto.getProductId(), ex);
		throw new RuntimeException("Failed to update product: database issue");
	}
	
	@Override
	public ProductDto update(final Integer productId, final ProductDto productDto) {
		log.info("*** ProductDto, service; update product with productId *");
		return ProductMappingHelper.map(this.productRepository
				.save(ProductMappingHelper.map(this.findById(productId))));
	}
	
	/**
	 * Eliminar producto con Retry
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2.0)
	)
	public void deleteById(final Integer productId) {
		log.info("🗑️ [Attempt] Deleting product: {}", productId);
		this.productRepository.delete(ProductMappingHelper
				.map(this.findById(productId)));
	}
	
	@Recover
	public void recoverDeleteById(DataAccessException ex, Integer productId) {
		log.error("❌ [Recover] Failed to delete product after retries: {}", productId, ex);
		throw new RuntimeException("Failed to delete product: database issue");
	}
	
}









