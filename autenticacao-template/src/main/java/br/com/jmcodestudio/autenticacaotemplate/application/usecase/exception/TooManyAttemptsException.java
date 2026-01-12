package br.com.jmcodestudio.autenticacaotemplate.application.usecase.exception;

/**
 * Exception thrown to indicate that a user or system has exceeded the allowable
 * number of attempts for a specific operation or process.
 *
 * This exception is commonly used in scenarios involving rate limiting,
 * failed authentication attempts, or repeated invalid actions to prevent abuse
 * or enhance security.
 *
 * Usage Context:
 * - Typically raised within workflows that implement account lockout after multiple
 *   failed login attempts or rate-limiting mechanisms.
 * - Commonly used to ensure security or safeguard system resources by enforcing
 *   restrictions on repetitive operations.
 *
 * Best Practices:
 * - Catch this exception at higher layers in the application to notify users
 *   or log the event appropriately.
 * - Combine with mechanisms to enforce retry limits or cooldowns for affected users.
 *
 * Related Classes:
 * - {@link br.com.megabarros.backendmodule.application.usecase.RefreshTokenUseCaseImpl}:
 *   May utilize this exception as part of workflows to limit retry abuse during token operations.
 * - Other authentication or rate-limiting mechanisms might throw this exception
 *   when encountering repeated invalid attempts.
 *
 *   Exceção para excesso de tentativas
 */
public class TooManyAttemptsException extends RuntimeException {
}
