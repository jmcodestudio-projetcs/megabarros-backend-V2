package br.com.jmcodestudio.megabarros.application.usecase.usuario;

import br.com.jmcodestudio.megabarros.application.policy.PasswordPolicy;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.CreateUsuarioUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.DeleteUsuarioUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.GetUsuarioUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.ListUsuariosUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.UpdateUsuarioUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.PasswordHasherPort;
import br.com.jmcodestudio.megabarros.application.port.out.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioUseCasesImpl implements CreateUsuarioUseCase, ListUsuariosUseCase, GetUsuarioUseCase, DeleteUsuarioUseCase, UpdateUsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(UsuarioUseCasesImpl.class);

    private final UsuarioRepositoryPort usuarios;
    private final PasswordHasherPort hasher;
    private final PasswordPolicy policy;
    private final CurrentUserPort currentUser;

    public UsuarioUseCasesImpl(UsuarioRepositoryPort usuarios,
                               PasswordHasherPort hasher,
                               PasswordPolicy policy,
                               CurrentUserPort currentUser) {
        this.usuarios = usuarios;
        this.hasher = hasher;
        this.policy = policy;
        this.currentUser = currentUser;
    }

    private boolean isAdmin() {
        String role = currentUser.role();
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

    private boolean isAdminOrUsuario() {
        String role = currentUser.role();
        return role != null && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("USUARIO"));
    }

    @Override
    public UsuarioRepositoryPort.UsuarioRecord create(UsuarioCreateCommand command) {
        String actor = currentUser.username();
        if (!isAdmin()) {
            log.warn("usuario.create denied actor={} role={}", actor, currentUser.role());
            throw new AccessDeniedException("Apenas ADMIN pode criar usuários.");
        }

        usuarios.findByEmail(command.email()).ifPresent(u -> {
            throw new IllegalStateException("Email já cadastrado.");
        });

        policy.validateOrThrow(command.senha());

        String hash = hasher.hash(command.senha());
        boolean ativo = command.ativo() != null ? command.ativo() : true;
        boolean mustChange = command.mustChangePassword() != null ? command.mustChangePassword() : false;

        var novo = new UsuarioRepositoryPort.UsuarioRecord(
                null,
                command.nome(),
                command.email(),
                hash,
                command.perfil(),
                ativo,
                mustChange
        );

        return usuarios.save(novo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRepositoryPort.UsuarioRecord> listAll() {
        String actor = currentUser.username();
        if (!isAdminOrUsuario()) {
            log.warn("usuario.listAll denied actor={} role={}", actor, currentUser.role());
            throw new AccessDeniedException("Apenas ADMIN/USUARIO podem listar usuários.");
        }
        return usuarios.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioRepositoryPort.UsuarioRecord> getById(Long id) {
        String actor = currentUser.username();
        if (!isAdminOrUsuario()) {
            log.warn("usuario.getById denied actor={} role={}", actor, currentUser.role());
            throw new AccessDeniedException("Apenas ADMIN/USUARIO podem consultar usuários.");
        }
        return usuarios.findById(id);
    }

    @Override
    public boolean deleteById(Long id) {
        String actor = currentUser.username();
        if (!isAdmin()) {
            log.warn("usuario.delete denied actor={} role={}", actor, currentUser.role());
            throw new AccessDeniedException("Apenas ADMIN pode excluir usuários.");
        }
        return usuarios.deleteById(id);
    }

    @Override
    public Optional<UsuarioRepositoryPort.UsuarioRecord> update(Long id, UsuarioUpdateCommand command) {
        String actor = currentUser.username();
        if (!isAdmin()) {
            log.warn("usuario.update denied actor={} role={}", actor, currentUser.role());
            throw new AccessDeniedException("Apenas ADMIN pode editar usuários.");
        }

        return usuarios.findById(id).map(existing -> {
            if (command.email() != null && !command.email().equalsIgnoreCase(existing.email())) {
                usuarios.findByEmail(command.email()).ifPresent(found -> {
                    if (!found.id().equals(id)) {
                        throw new IllegalStateException("Email já cadastrado.");
                    }
                });
            }

            String newHash = existing.senhaHash();
            if (command.senha() != null) {
                policy.validateOrThrow(command.senha());
                newHash = hasher.hash(command.senha());
            }

            var updated = new UsuarioRepositoryPort.UsuarioRecord(
                    existing.id(),
                    command.nome() != null ? command.nome() : existing.nome(),
                    command.email() != null ? command.email() : existing.email(),
                    newHash,
                    command.perfil() != null ? command.perfil() : existing.perfil(),
                    command.ativo() != null ? command.ativo() : existing.ativo(),
                    command.mustChangePassword() != null ? command.mustChangePassword() : existing.mustChangePassword()
            );

            return usuarios.save(updated);
        });
    }
}