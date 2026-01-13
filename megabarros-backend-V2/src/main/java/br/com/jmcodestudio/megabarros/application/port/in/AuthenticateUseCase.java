package br.com.jmcodestudio.megabarros.application.port.in;

/**
 * The AuthenticateUseCase interface defines the contract for authenticating users and handling login functionality.
 * O que faz: Define o contrato do caso de uso de login.
 * Por que: O controller chama esse port; a implementação não conhece HTTP, só regras
 */
public interface AuthenticateUseCase {
    AuthResult login(String email, String rawPassword);

    record AuthResult(Long userId, String email, String role, String accessToken, String refreshToken) {}
}
