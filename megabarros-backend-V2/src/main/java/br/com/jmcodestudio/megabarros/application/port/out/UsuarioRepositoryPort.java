package br.com.jmcodestudio.megabarros.application.port.out;

import java.util.Optional;

/**
 * Interface representing the repository port for user-related data operations.
 * Provides methods to interact with user records and perform CRUD-like operations.
 * Abstrai consultas/gravações de usuário.
 * Por que: a implementação JPA fica no adapter; casos de uso dependem só desta interface
 */
public interface UsuarioRepositoryPort {
    Optional<UsuarioRecord> findByEmail(String email);
    Optional<UsuarioRecord> findById(Long id);
    UsuarioRecord save(UsuarioRecord usuario);
    void updatePassword(Long id, String newHash, boolean mustChangePassword);

    record UsuarioRecord(Long id, String nome, String email, String senhaHash, String perfil, boolean ativo, boolean mustChangePassword) {}
}