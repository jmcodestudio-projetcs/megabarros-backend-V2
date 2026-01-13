package br.com.jmcodestudio.megabarros.application.usecase;

import br.com.jmcodestudio.megabarros.application.port.in.AuthenticateUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.RefreshTokenUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.*;
import br.com.jmcodestudio.megabarros.application.usecase.exception.TokenInvalidException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Implementation of the RefreshTokenUseCase interface.
 * This class handles the business logic for refreshing access tokens using a secure token rotation approach.
 * Fluxo: valida refresh JWT → busca hash na store → checa revogação/expiração → gera novo refresh (rotação) → revoga o antigo → gera novo access.
 * Segurança: rotação a cada uso e invalidação do anterior.
 */

@Service
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final TokenServicePort tokens;
    private final RefreshTokenStorePort refreshStore;
    private final UsuarioRepositoryPort usuarios;
    private final AuditLogPort audit;
    private final RequestMetadataPort req;

    public RefreshTokenUseCaseImpl(TokenServicePort tokens,
                                   RefreshTokenStorePort refreshStore,
                                   UsuarioRepositoryPort usuarios,
                                   AuditLogPort audit,
                                   RequestMetadataPort req) {
        this.tokens = tokens;
        this.refreshStore = refreshStore;
        this.usuarios = usuarios;
        this.audit = audit;
        this.req = req;
    }

    @Override
    public AuthenticateUseCase.AuthResult refresh(String refreshToken) {
        var now = Instant.now();
        try {
            var claims = tokens.parseAndValidateRefresh(refreshToken);

            var hash = AuthenticateUseCaseImpl.HashUtils.sha256(refreshToken);
            var stored = refreshStore.findByHash(hash).orElseThrow(TokenInvalidException::new);

            if (stored.revokedAt() != null || stored.expiresAt().isBefore(now)) {
                throw new TokenInvalidException();
            }

            var newRefresh = tokens.generateRefreshToken(stored.userId(), now);
            var newHash = AuthenticateUseCaseImpl.HashUtils.sha256(newRefresh.token());
            refreshStore.rotate(hash, newHash, newRefresh.jti(), now, newRefresh.expiresAt());

            var user = usuarios.findById(stored.userId()).orElseThrow(TokenInvalidException::new);
            var access = tokens.generateAccessToken(user.id(), user.email(), user.perfil(), Map.of("iss", "megabarros-v2"), now);

            audit.record(new AuditLogPort.Entry(
                    now, user.id(), "REFRESH_SUCCESS", user.email(), req.ip(), req.userAgent(), Map.of()
            ));

            return new AuthenticateUseCase.AuthResult(user.id(), user.email(), user.perfil(), access, newRefresh.token());
        } catch (TokenInvalidException ex) {
            audit.record(new AuditLogPort.Entry(
                    now, null, "REFRESH_FAILED", null, req.ip(), req.userAgent(), Map.of()
            ));
            throw ex;
        }
    }

}
