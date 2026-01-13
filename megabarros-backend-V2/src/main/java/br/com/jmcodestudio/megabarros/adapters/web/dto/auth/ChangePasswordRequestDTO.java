package br.com.jmcodestudio.megabarros.adapters.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Encapsulates the data required to process a change password request.
 *
 * This Data Transfer Object (DTO) is utilized to transfer information
 * necessary for a password change operation, including the user's
 * current password and a new desired password.
 *
 * Definem payloads HTTP (entrada/saída). Isolam a borda web do core
 */
public record ChangePasswordRequestDTO(@NotBlank String currentPassword, @NotBlank String newPassword) {}