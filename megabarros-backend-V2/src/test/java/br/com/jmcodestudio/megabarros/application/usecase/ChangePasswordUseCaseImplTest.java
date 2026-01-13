package br.com.jmcodestudio.megabarros.application.usecase;

import br.com.jmcodestudio.megabarros.application.policy.PasswordPolicy;
import br.com.jmcodestudio.megabarros.application.policy.PasswordPolicy.WeakPasswordException;
import br.com.jmcodestudio.megabarros.application.port.out.*;
import br.com.jmcodestudio.megabarros.application.usecase.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangePasswordUseCaseImplTest {

    UsuarioRepositoryPort usuarios;
    PasswordHasherPort hasher;
    RefreshTokenStorePort refreshStore;
    AuditLogPort audit;
    PasswordPolicy policy;

    ChangePasswordUseCaseImpl useCase;

    @BeforeEach
    void setup() {
        usuarios = mock(UsuarioRepositoryPort.class);
        hasher = mock(PasswordHasherPort.class);
        refreshStore = mock(RefreshTokenStorePort.class);
        audit = mock(AuditLogPort.class);
        policy = mock(PasswordPolicy.class);

        useCase = new ChangePasswordUseCaseImpl(usuarios, hasher, refreshStore, audit, policy);
    }

    @Test
    void changePassword_success_revokesAllRefreshTokens() {
        var user = new UsuarioRepositoryPort.UsuarioRecord(1L,"Admin","admin@example.com","hash","ADMIN",true,false);
        when(usuarios.findById(1L)).thenReturn(Optional.of(user));
        when(hasher.matches("Admin@123", "hash")).thenReturn(true);

        // política aceita
        doNothing().when(policy).validateOrThrow("NewStrong@123");

        // IMPORTANTE: stub de hash para não retornar null
        when(hasher.hash("NewStrong@123")).thenReturn("hashed");

        useCase.changePassword(1L,"Admin@123","NewStrong@123");

        verify(usuarios).updatePassword(eq(1L), eq("hashed"), eq(false));
        verify(refreshStore).revokeAllByUserId(eq(1L), eq("password_change"), any(Instant.class));
        verify(audit).record(any());
    }

    @Test
    void changePassword_fails_when_current_mismatch() {
        var user = new UsuarioRepositoryPort.UsuarioRecord(1L,"Admin","admin@example.com","hash","ADMIN",true,false);
        when(usuarios.findById(1L)).thenReturn(Optional.of(user));
        when(hasher.matches("Wrong", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> useCase.changePassword(1L,"Wrong","NewStrong@123"));
        verify(usuarios, never()).updatePassword(anyLong(), anyString(), anyBoolean());
        verify(refreshStore, never()).revokeAllByUserId(anyLong(), anyString(), any(Instant.class));
    }

    @Test
    void changePassword_fails_when_policy_rejects() {
        var user = new UsuarioRepositoryPort.UsuarioRecord(1L,"Admin","admin@example.com","hash","ADMIN",true,false);
        when(usuarios.findById(1L)).thenReturn(Optional.of(user));
        when(hasher.matches("Admin@123", "hash")).thenReturn(true);

        doThrow(new WeakPasswordException("weak")).when(policy).validateOrThrow("weak");

        assertThrows(WeakPasswordException.class, () -> useCase.changePassword(1L,"Admin@123","weak"));
        verify(usuarios, never()).updatePassword(anyLong(), anyString(), anyBoolean());
        verify(refreshStore, never()).revokeAllByUserId(anyLong(), anyString(), any(Instant.class));
    }
}