package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.exception.wrapper.CredentialNotFoundException;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.helper.CredentialMappingHelper;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.service.CredentialService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {
	
	private final CredentialRepository credentialRepository;
	
	/**
	 * Listar credenciales con Retry
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 2,
		backoff = @Backoff(delay = 500)
	)
	public List<CredentialDto> findAll() {
		log.info("🔐 [Attempt] Finding all credentials");
		return this.credentialRepository.findAll()
				.stream()
					.map(CredentialMappingHelper::map)
					.distinct()
					.collect(Collectors.toUnmodifiableList());
	}
	
	/**
	 * Buscar credencial por ID con Retry
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 2,
		backoff = @Backoff(delay = 500)
	)
	public CredentialDto findById(final Integer credentialId) {
		log.info("🔐 [Attempt] Finding credential: {}", credentialId);
		return this.credentialRepository.findById(credentialId)
				.map(CredentialMappingHelper::map)
				.orElseThrow(() -> new CredentialNotFoundException(String.format("#### Credential with id: %d not found! ####", credentialId)));
	}
	
	/**
	 * Guardar credencial con Retry para deadlocks
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2.0)
	)
	public CredentialDto save(final CredentialDto credentialDto) {
		log.info("💾 [Attempt] Saving credential for username: {}", credentialDto.getUsername());
		return CredentialMappingHelper.map(this.credentialRepository.save(CredentialMappingHelper.map(credentialDto)));
	}
	
	@Recover
	public CredentialDto recoverSave(DataAccessException ex, CredentialDto credentialDto) {
		log.error("❌ [Recover] Failed to save credential after retries: {}", 
			credentialDto.getUsername(), ex);
		throw new RuntimeException("Failed to save credential: database issue");
	}
	
	/**
	 * Actualizar credencial con Retry
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2.0)
	)
	public CredentialDto update(final CredentialDto credentialDto) {
		log.info("🔄 [Attempt] Updating credential: {}", credentialDto.getCredentialId());
		return CredentialMappingHelper.map(this.credentialRepository.save(CredentialMappingHelper.map(credentialDto)));
	}
	
	@Recover
	public CredentialDto recoverUpdate(DataAccessException ex, CredentialDto credentialDto) {
		log.error("❌ [Recover] Failed to update credential after retries: {}", 
			credentialDto.getCredentialId(), ex);
		throw new RuntimeException("Failed to update credential: database issue");
	}
	
	@Override
	public CredentialDto update(final Integer credentialId, final CredentialDto credentialDto) {
		log.info("*** CredentialDto, service; update credential with credentialId *");
		return CredentialMappingHelper.map(this.credentialRepository.save(
				CredentialMappingHelper.map(this.findById(credentialId))));
	}
	
	/**
	 * Eliminar credencial con Retry
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 1000, multiplier = 2.0)
	)
	public void deleteById(final Integer credentialId) {
		log.info("🗑️ [Attempt] Deleting credential: {}", credentialId);
		this.credentialRepository.deleteById(credentialId);
	}
	
	@Recover
	public void recoverDeleteById(DataAccessException ex, Integer credentialId) {
		log.error("❌ [Recover] Failed to delete credential after retries: {}", credentialId, ex);
		throw new RuntimeException("Failed to delete credential: database issue");
	}
	
	/**
	 * Buscar credencial por username con Retry
	 * CRÍTICO: Usado en autenticación
	 */
	@Override
	@Retryable(
		value = { DataAccessException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 500, multiplier = 2.0)
	)
	public CredentialDto findByUsername(final String username) {
		log.info("🔐 [Attempt] Finding credential for username: {}", username);
		return CredentialMappingHelper.map(this.credentialRepository.findByUsername(username)
				.orElseThrow(() -> new UserObjectNotFoundException(String.format("#### Credential with username: %s not found! ####", username))));
	}
	
	@Recover
	public CredentialDto recoverFindByUsername(DataAccessException ex, String username) {
		log.error("❌ [Recover] Failed to find credential after retries: {}", username, ex);
		throw new RuntimeException("Authentication service temporarily unavailable");
	}
	
}










