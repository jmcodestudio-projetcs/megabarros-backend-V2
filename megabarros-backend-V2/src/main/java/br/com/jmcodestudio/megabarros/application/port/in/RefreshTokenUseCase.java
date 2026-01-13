package br.com.jmcodestudio.megabarros.application.port.in;

/**
 * Interface representing the use case for refreshing user access tokens.
 * O que faz: Define o contrato do caso de uso de refresh token.
 * Por que: Renova access token usando refresh token com rotação segura
 */
public interface RefreshTokenUseCase {
    AuthenticateUseCase.AuthResult refresh(String refreshToken);
}