package br.com.jmcodestudio.megabarros.adapters.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents the data required for processing a login request.
 *
 * This Data Transfer Object (DTO) is used to encapsulate the necessary
 * information for a login operation, including the user's email and password.
 *
 * Utilized as an HTTP payload to isolate the web boundary from the core.
 *
 * Definem payloads HTTP (entrada/saída). Isolam a borda web do core
 */
public record LoginRequestDTO(
        @Email @NotBlank String email,
        @NotBlank String senha
) {}