package br.com.jmcodestudio.megabarros.application.usecase;

import br.com.jmcodestudio.megabarros.application.port.in.AuthenticateUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.*;
import br.com.jmcodestudio.megabarros.application.usecase.exception.InvalidCredentialsException;
import br.com.jmcodestudio.megabarros.application.usecase.exception.TooManyAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticateUseCaseImplTest {

    UsuarioRepositoryPort usuarios;
    PasswordHasherPort hasher;
    TokenServicePort tokens;
    RefreshTokenStorePort refreshStore;
    AuditLogPort audit;
    RequestMetadataPort req;
    LoginRateLimiterPort limiter;

    AuthenticateUseCaseImpl useCase;

    @BeforeEach
    void setup() {
        usuarios = mock(UsuarioRepositoryPort.class);
        hasher = mock(PasswordHasherPort.class);
        tokens = mock(TokenServicePort.class);
        refreshStore = mock(RefreshTokenStorePort.class);
        audit = mock(AuditLogPort.class);
        req = mock(RequestMetadataPort.class);
        limiter = mock(LoginRateLimiterPort.class);

        useCase = new AuthenticateUseCaseImpl(usuarios, hasher, tokens, refreshStore, audit, req, limiter);
    }

    @Test
    void login_success_with_rate_limit_allowed() {
        when(req.ip()).thenReturn("127.0.0.1");
        when(limiter.allow(anyString())).thenReturn(true);

        var user = new UsuarioRepositoryPort.UsuarioRecord(1L,"Admin","admin@example.com","hash","ADMIN",true,false);
        when(usuarios.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(hasher.matches("Admin@123", "hash")).thenReturn(true);

        when(tokens.generateAccessToken(eq(1L), eq("admin@example.com"), eq("ADMIN"), any(Map.class), any(Instant.class)))
                .thenReturn("access");
        var genRefresh = new TokenServicePort.GeneratedRefresh("refresh", "jti-new", Instant.now().plusSeconds(100));
        when(tokens.generateRefreshToken(eq(1L), any(Instant.class))).thenReturn(genRefresh);

        AuthenticateUseCase.AuthResult result = useCase.login("admin@example.com","Admin@123");

        assertEquals(1L, result.userId());
        assertEquals("access", result.accessToken());
        assertEquals("refresh", result.refreshToken());
        verify(refreshStore).persist(eq(1L), anyString(), eq("jti-new"), any(Instant.class));
        verify(limiter).recordSuccess(anyString());
        verify(audit, atLeastOnce()).record(any());
    }

    @Test
    void login_rate_limited() {
        when(req.ip()).thenReturn("127.0.0.1");
        when(limiter.allow(anyString())).thenReturn(false);

        assertThrows(TooManyAttemptsException.class, () -> useCase.login("admin@example.com","Admin@123"));
        verify(audit).record(any()); // LOGIN_RATE_LIMIT
    }

    @Test
    void login_invalid_credentials_increments_failures_and_audits() {
        when(req.ip()).thenReturn("127.0.0.1");
        when(limiter.allow(anyString())).thenReturn(true);

        var user = new UsuarioRepositoryPort.UsuarioRecord(1L,"Admin","admin@example.com","hash","ADMIN",true,false);
        when(usuarios.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(hasher.matches("wrong", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> useCase.login("admin@example.com","wrong"));
        verify(limiter).recordFailure(anyString());
        verify(audit).record(any()); // LOGIN_FAILED
    }
}