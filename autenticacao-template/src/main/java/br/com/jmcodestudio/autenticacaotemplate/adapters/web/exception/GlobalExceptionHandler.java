package br.com.jmcodestudio.autenticacaotemplate.adapters.web.exception;

import br.com.jmcodestudio.autenticacaotemplate.application.policy.ForbiddenException;
import br.com.jmcodestudio.autenticacaotemplate.application.policy.PasswordPolicy;
import br.com.jmcodestudio.autenticacaotemplate.application.usecase.exception.InvalidCredentialsException;
import br.com.jmcodestudio.autenticacaotemplate.application.usecase.exception.TokenInvalidException;
import br.com.jmcodestudio.autenticacaotemplate.application.usecase.exception.TooManyAttemptsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Centralized exception handler to manage application-wide exception responses.
 *
 * This class uses the {@code @RestControllerAdvice} annotation to intercept and handle
 * exceptions globally for REST controllers. It provides predefined HTTP status codes
 * and response bodies for specific exceptions to maintain consistent error handling
 * across the application.
 *
 * Responsibilities:
 * - Handle {@link InvalidCredentialsException} by returning a 401 Unauthorized response
 *   with an error message indicating invalid credentials.
 * - Handle {@link TokenInvalidException} by returning a 401 Unauthorized response with
 *   an error message indicating an invalid refresh token.
 *
 * Key Features:
 * - Ensures standardization of error responses for authentication-related failures.
 * - Provides clear and structured error information for unauthorized access scenarios.
 *
 * Exceptions Handled:
 * - {@link InvalidCredentialsException}: Indicates that the provided user credentials
 *   are invalid during authentication attempts.
 * - {@link TokenInvalidException}: Indicates that a provided token is invalid, such as
 *   expired or improperly formatted tokens.
 *
 *   O que faz: converte exceções do core em respostas HTTP padronizadas.
 *   Mapeia exceções de segurança para 401/403/429 e validação de senha para 400.
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> invalidCredentials() {
        return ResponseEntity.status(401).body(Map.of("error","invalid_credentials"));
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<?> invalidToken() {
        return ResponseEntity.status(401).body(Map.of("error","invalid_refresh_token"));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> forbidden(ForbiddenException ex) {
        return ResponseEntity.status(403).body(Map.of("error","forbidden", "message", ex.getMessage()));
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<?> tooMany() {
        return ResponseEntity.status(429).body(Map.of("error","too_many_attempts"));
    }

    @ExceptionHandler(PasswordPolicy.WeakPasswordException.class)
    public ResponseEntity<?> weakPassword(PasswordPolicy.WeakPasswordException ex) {
        return ResponseEntity.badRequest().body(Map.of("error","weak_password", "message", ex.getMessage()));
    }
}