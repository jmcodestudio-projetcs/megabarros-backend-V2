package br.com.jmcodestudio.autenticacaotemplate.application.usecase;

import br.com.jmcodestudio.autenticacaotemplate.application.port.in.AuthenticateUseCase;
import br.com.jmcodestudio.autenticacaotemplate.application.port.out.*;
import br.com.jmcodestudio.autenticacaotemplate.application.usecase.exception.InvalidCredentialsException;
import br.com.jmcodestudio.autenticacaotemplate.application.usecase.exception.TooManyAttemptsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Implementation of the {@link AuthenticateUseCase} interface responsible for handling user authentication and login logic.
 *
 * This class orchestrates the authentication process by verifying user credentials, generating access and refresh tokens,
 * and managing token persistence. It interacts with various ports including the user repository, password hashing service,
 * token service, and refresh token store to perform these operations in a decoupled and modular manner.
 *
 * Responsibilities:
 * - Validate user credentials against the stored user data.
 * - Generate access and refresh tokens upon successful authentication.
 * - Persist refresh tokens securely for subsequent use.
 * - Throw an {@code InvalidCredentialsException} if authentication fails.
 *
 * Dependencies:
 * - {@link UsuarioRepositoryPort}: Abstracts access to user data.
 * - {@link PasswordHasherPort}: Handles password hash validation.
 * - {@link TokenServicePort}: Generates access and refresh tokens.
 * - {@link RefreshTokenStorePort}: Manages the storage of refresh tokens.
 *
 * Exceptions:
 * - {@link br.com.jmcodestudio.autenticacaotemplate.application.usecase.exception.InvalidCredentialsException}:
 *   Thrown when the credentials provided are invalid.
 *
 *   Fluxo: busca usuário por email → valida ativo e senha → gera access e refresh → salva hash do refresh → retorna tokens.
 *   Segurança: refresh token é persistido como hash (SHA-256).
 *
 *   Use case de login: aplica rate limiter/lockout; gera tokens; persiste refresh; audita sucesso/falha.
 */
@Service
public class AuthenticateUseCaseImpl implements AuthenticateUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;
    private final TokenServicePort tokens;
    private final RefreshTokenStorePort refreshStore;
    private final AuditLogPort audit;
    private final RequestMetadataPort req;
    private final LoginRateLimiterPort limiter;

    public AuthenticateUseCaseImpl(UsuarioRepositoryPort usuarios,
                                   PasswordHasherPort hasher,
                                   TokenServicePort tokens,
                                   RefreshTokenStorePort refreshStore,
                                   AuditLogPort audit,
                                   RequestMetadataPort req,
                                   LoginRateLimiterPort limiter) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.tokens = tokens;
        this.refreshStore = refreshStore;
        this.audit = audit;
        this.req = req;
        this.limiter = limiter;
    }

    @Override
    public AuthResult login(String email, String rawPassword) {
        var now = Instant.now();
        var key = (req.ip() != null ? req.ip() : "unknown") + "|" + email;

        if (!limiter.allow(key)) {
            audit.record(new AuditLogPort.Entry(now, null, "LOGIN_RATE_LIMIT", email, req.ip(), req.userAgent(), Map.of()));
            throw new TooManyAttemptsException();
        }

        try {
            var user = usuarios.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
            if (!user.ativo() || !hasher.matches(rawPassword, user.senhaHash())) {
                limiter.recordFailure(key);
                throw new InvalidCredentialsException();
            }

            var access = tokens.generateAccessToken(user.id(), user.email(), user.perfil(), Map.of("iss", "megabarros-v2"), now);
            var refresh = tokens.generateRefreshToken(user.id(), now);

            var hash = HashUtils.sha256(refresh.token());
            refreshStore.persist(user.id(), hash, refresh.jti(), refresh.expiresAt());

            limiter.recordSuccess(key);

            audit.record(new AuditLogPort.Entry(
                    now, user.id(), "LOGIN_SUCCESS", email, req.ip(), req.userAgent(),
                    Map.of("role", user.perfil())
            ));

            return new AuthResult(user.id(), user.email(), user.perfil(), access, refresh.token());
        } catch (InvalidCredentialsException ex) {
            audit.record(new AuditLogPort.Entry(
                    now, null, "LOGIN_FAILED", email, req.ip(), req.userAgent(), Map.of()
            ));
            throw ex;
        }
    }

    static class HashUtils {
        static String sha256(String input) {
            try {
                var md = java.security.MessageDigest.getInstance("SHA-256");
                var digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                var sb = new StringBuilder();
                for (byte b : digest) sb.append(String.format("%02x", b));
                return sb.toString();
            } catch (Exception e) {
                throw new IllegalStateException("Hash error", e);
            }
        }
    }
}