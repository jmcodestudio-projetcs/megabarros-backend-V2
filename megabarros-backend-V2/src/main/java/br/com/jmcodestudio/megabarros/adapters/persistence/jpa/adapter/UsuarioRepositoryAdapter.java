package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.UsuarioEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.UsuarioJpaRepository;
import br.com.jmcodestudio.megabarros.application.port.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adapter implementation for the {@code UsuarioRepositoryPort} interface that bridges
 * the application domain with the persistence layer by leveraging JPA.
 * This class provides methods for user-related database operations such as find, save, and update.
 *
 * Responsibilities:
 * 1. Querying user records by email or id.
 * 2. Persisting or updating user records in the database.
 * 3. Mapping between domain objects ({@code UsuarioRecord}) and persistence entities
 *    ({@code UsuarioEntity}).
 *
 * Key features:
 * - Maps {@code UsuarioEntity} to {@code UsuarioRecord} and vice versa.
 * - Annotated with {@code @Component}, enabling Spring to manage its lifecycle.
 * - Delegates database interactions to an underlying JPA repository ({@code UsuarioJpaRepository}).
 * - Ensures transactional integrity for create and update operations through the {@code @Transactional} annotation.
 *
 * O que fazem: convertem entre entidade JPA e record do port e implementam operações exigidas pelos casos de uso.
 */
@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repo;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<UsuarioRecord> findByEmail(String email) {
        return repo.findByEmail(email).map(this::map);
    }

    @Override
    public Optional<UsuarioRecord> findById(Long id) {
        return repo.findById(id).map(this::map);
    }

    @Override
    @Transactional
    public UsuarioRecord save(UsuarioRecord usuario) {
        var e = new UsuarioEntity();
        e.setId(usuario.id());
        e.setNome(usuario.nome());
        e.setEmail(usuario.email());
        e.setSenhaHash(usuario.senhaHash());
        e.setPerfil(usuario.perfil());
        e.setAtivo(usuario.ativo());
        e.setMustChangePassword(usuario.mustChangePassword());
        var saved = repo.save(e);
        return map(saved);
    }

    @Override
    @Transactional
    public void updatePassword(Long id, String newHash, boolean mustChangePassword) {
        var e = repo.findById(id).orElseThrow();
        e.setSenhaHash(newHash);
        e.setMustChangePassword(mustChangePassword);
        repo.save(e);
    }

    private UsuarioRecord map(UsuarioEntity e) {
        return new UsuarioRecord(
                e.getId(), e.getNome(), e.getEmail(), e.getSenhaHash(),
                e.getPerfil(), Boolean.TRUE.equals(e.getAtivo()), Boolean.TRUE.equals(e.getMustChangePassword())
        );
    }
}