package br.com.jmcodestudio.megabarros.application.usecase.exception;

/**
 * Exception thrown to indicate that a provided token is invalid.
 *
 * This exception is typically used in scenarios where token validation fails,
 * such as during authentication or token refresh processes.
 *
 * Usage Context:
 * - It is thrown when an invalid, expired, revoked, or otherwise unusable token
 *   is encountered in application workflows.
 * - Commonly used in cases like JWT validation, refresh token rotation, or other
 *   token-based mechanisms.
 *
 * Best Practices:
 * - Catch this exception at higher layers of the application, such as controllers
 *   or interceptors, to handle token-related errors gracefully.
 * - Provide meaningful error responses to clients without leaking sensitive
 *   implementation details.
 *
 * Related Classes:
 * - {@link br.com.megabarros.backendmodule.application.usecase.RefreshTokenUseCaseImpl}:
 *   Utilizes this exception when encountering invalid tokens during the token
 *   refresh process.
 * - Other token-related utility or service classes may also use this exception
 *   to communicate invalid token issues.
 *
 *   Comunica erros de autenticação para o handler HTTP.
 */
public class TokenInvalidException extends RuntimeException{
}
