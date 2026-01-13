package br.com.jmcodestudio.megabarros.application.usecase;

import br.com.jmcodestudio.megabarros.application.policy.PasswordPolicy;
import br.com.jmcodestudio.megabarros.application.port.in.ChangePasswordUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.AuditLogPort;
import br.com.jmcodestudio.megabarros.application.port.out.PasswordHasherPort;
import br.com.jmcodestudio.megabarros.application.port.out.RefreshTokenStorePort;
import br.com.jmcodestudio.megabarros.application.port.out.UsuarioRepositoryPort;
import br.com.jmcodestudio.megabarros.application.usecase.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Implementation of the {@link ChangePasswordUseCase} interface for handling user password change operations.
 *
 * Responsibilities:
 * - Validates the current password provided by the user.
 * - Updates the user's password with a newly hashed version.
 * - Revokes all refresh tokens for the user to enforce reauthentication after a password change.
 *
 * Dependencies:
 * - {@link UsuarioRepositoryPort}: Handles user-related data operations, such as fetching and updating user records.
 * - {@link PasswordHasherPort}: Provides functionality for hashing and verifying passwords.
 * - {@link RefreshTokenStorePort}: Manages operations related to refresh token storage and revocation.
 *
 * Throws:
 * - {@link InvalidCredentialsException}: If the current password provided does not match the stored password hash for the user.
 *
 * Fluxo: valida senha atual → grava nova hash → revoga todos os refresh tokens do usuário.
 * Segurança: evita que tokens antigos continuem válidos após troca de senha.
 *
 * Remove o downcast ao adapter; usa o novo método do port para revogar todos os refresh tokens do usuário.
 * Aplica política de senha; trocando a senha, revoga todos os refresh tokens e audita evento.
 */

@Service
public class ChangePasswordUseCaseImpl implements ChangePasswordUseCase {

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;
    private final RefreshTokenStorePort refreshStore;
    private final AuditLogPort audit;
    private final PasswordPolicy policy;

    public ChangePasswordUseCaseImpl(UsuarioRepositoryPort usuarios, PasswordHasherPort hasher,
                                     RefreshTokenStorePort refreshStore, AuditLogPort audit,
                                     PasswordPolicy policy) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.refreshStore = refreshStore;
        this.audit = audit;
        this.policy = policy;
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        var now = Instant.now();
        var user = usuarios.findById(userId).orElseThrow(InvalidCredentialsException::new);
        if (!hasher.matches(currentPassword, user.senhaHash())) {
            audit.record(new AuditLogPort.Entry(now, userId, "PASSWORD_CHANGE_FAILED", user.email(), null, null, Map.of("reason","mismatch")));
            throw new InvalidCredentialsException();
        }

        policy.validateOrThrow(newPassword);

        var newHash = hasher.hash(newPassword);
        usuarios.updatePassword(userId, newHash, false);

        refreshStore.revokeAllByUserId(userId, "password_change", now);

        audit.record(new AuditLogPort.Entry(now, userId, "PASSWORD_CHANGE_SUCCESS", user.email(), null, null, Map.of()));
    }

}
