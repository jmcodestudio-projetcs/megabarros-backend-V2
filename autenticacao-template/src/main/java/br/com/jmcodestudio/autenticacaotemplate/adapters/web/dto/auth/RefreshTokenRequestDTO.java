package br.com.jmcodestudio.autenticacaotemplate.adapters.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Encapsulates the data required to process a refresh token request.
 *
 * This Data Transfer Object (DTO) is designed to transfer the refresh token
 * necessary for obtaining a new access token during token renewal operations.
 *
 * Serves as an HTTP payload to isolate the web boundary from the core.
 *
 * Definem payloads HTTP (entrada/saída). Isolam a borda web do core
 */
public record RefreshTokenRequestDTO(@NotBlank String refreshToken) {}