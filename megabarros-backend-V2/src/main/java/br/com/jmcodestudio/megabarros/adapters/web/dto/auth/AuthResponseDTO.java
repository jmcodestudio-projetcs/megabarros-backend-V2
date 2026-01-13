package br.com.jmcodestudio.megabarros.adapters.web.dto.auth;

/**
 * Represents the response data for an authentication request.
 *
 * This DTO (Data Transfer Object) is utilized to encapsulate the details
 * returned after a successful authentication process, including user
 * identification, role, and token information.
 *
 * Attributes include:
 * - userId: The identifier of the authenticated user.
 * - email: The email address associated with the authenticated user.
 * - role: The role assigned to the authenticated user.
 * - accessToken: The access token issued for authenticated operations.
 * - refreshToken: The refresh token issued for obtaining new access tokens.
 *
 * Definem payloads HTTP (entrada/saída). Isolam a borda web do core
 */
public record AuthResponseDTO(Long userId, String email, String role, String accessToken, String refreshToken) {}