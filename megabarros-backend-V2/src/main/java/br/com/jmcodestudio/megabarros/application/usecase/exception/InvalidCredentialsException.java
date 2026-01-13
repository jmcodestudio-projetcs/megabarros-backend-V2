package br.com.jmcodestudio.megabarros.application.usecase.exception;

/**
 * Exception thrown to indicate that user credentials provided during an authentication
 * attempt are invalid.
 *
 * This exception is typically used in scenarios where authentication fails due to
 * incorrect username, password, or account status issues (e.g., deactivated accounts).
 *
 * Usage Context:
 * - It is used in authentication procedures to signify invalid login attempts caused
 *   by failed credential validation.
 * - Commonly thrown in implementations of authentication use cases, such as within
 *   login or token generation methods, when the supplied credentials do not match
 *   stored user details.
 *
 * Best Practices:
 * - Catch this exception at a higher layer (e.g., a controller or service) to provide
 *   user-friendly error messages or responses.
 * - Handle this exception appropriately to avoid exposing sensitive application details.
 *
 * Related Classes:
 * - {@link br.com.megabarros.backendmodule.application.usecase.AuthenticateUseCaseImpl}:
 *   Throws this exception when a user's credentials validation fails.
 * - {@link RefreshTokenUseCaseImpl}: May interact with related exceptions during token use.
 *
 * Comunica erros de autenticação para o handler HTTP.
 */
public class InvalidCredentialsException extends RuntimeException{
}
