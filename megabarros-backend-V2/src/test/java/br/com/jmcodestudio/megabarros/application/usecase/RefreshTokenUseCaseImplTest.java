package br.com.jmcodestudio.megabarros.application.usecase;

import br.com.jmcodestudio.megabarros.application.port.in.AuthenticateUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.RefreshTokenUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.*;
import br.com.jmcodestudio.megabarros.application.usecase.exception.TokenInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenUseCaseImplTest {

    TokenServicePort tokens;
    RefreshTokenStorePort refreshStore;
    UsuarioRepositoryPort usuarios;
    AuditLogPort audit;
    RequestMetadataPort req;

    RefreshTokenUseCase useCase;

    @BeforeEach
    void setup() {
        tokens = mock(TokenServicePort.class);
        refreshStore = mock(RefreshTokenStorePort.class);
        usuarios = mock(UsuarioRepositoryPort.class);
        audit = mock(AuditLogPort.class);
        req = mock(RequestMetadataPort.class);

        useCase = new RefreshTokenUseCaseImpl(tokens, refreshStore, usuarios, audit, req);
    }

    @Test
    void refresh_success_rotates_with_new_jti() {
        when(tokens.parseAndValidateRefresh("R")).thenReturn(new TokenServicePort.Claims(1L,"admin@example.com","ADMIN","jti-old", Instant.now().plusSeconds(100)));
        when(refreshStore.findByHash(anyString())).thenReturn(Optional.of(
                new RefreshTokenStorePort.RefreshTokenRecord(10L,1L,"oldHash","jti-old",Instant.now(),Instant.now().plusSeconds(100),null,null,null)
        ));
        when(usuarios.findById(1L)).thenReturn(Optional.of(new UsuarioRepositoryPort.UsuarioRecord(1L,"Admin","admin@example.com","hash","ADMIN",true,false)));

        var newRefresh = new TokenServicePort.GeneratedRefresh("R2","jti-new", Instant.now().plusSeconds(200));
        when(tokens.generateRefreshToken(eq(1L), any(Instant.class))).thenReturn(newRefresh);
        when(tokens.generateAccessToken(eq(1L), eq("admin@example.com"), eq("ADMIN"), any(), any())).thenReturn("A2");

        AuthenticateUseCase.AuthResult result = useCase.refresh("R");

        assertEquals("A2", result.accessToken());
        assertEquals("R2", result.refreshToken());
        verify(refreshStore).rotate(anyString(), anyString(), eq("jti-new"), any(Instant.class), any(Instant.class));
        verify(audit).record(any()); // REFRESH_SUCCESS
    }

    @Test
    void refresh_invalid_token_causes_audit_failed() {
        when(tokens.parseAndValidateRefresh("bad")).thenThrow(new TokenInvalidException());

        assertThrows(TokenInvalidException.class, () -> useCase.refresh("bad"));
        verify(audit).record(any()); // REFRESH_FAILED
    }

    @Test
    void refresh_revoked_stored_token_is_invalid() {
        when(tokens.parseAndValidateRefresh("R")).thenReturn(new TokenServicePort.Claims(1L,"admin@example.com","ADMIN","jti-old", Instant.now().plusSeconds(100)));
        when(refreshStore.findByHash(anyString())).thenReturn(Optional.of(
                new RefreshTokenStorePort.RefreshTokenRecord(10L,1L,"oldHash","jti-old",Instant.now(),Instant.now().plusSeconds(100),Instant.now(),null,"rotated")
        ));

        assertThrows(TokenInvalidException.class, () -> useCase.refresh("R"));
    }
}