package br.com.jmcodestudio.megabarros.application.usecase.corretor;

import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;
import br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId;
import br.com.jmcodestudio.megabarros.application.port.in.corretor.*;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorRepositoryPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CorretorUseCasesImpl implements
        CreateCorretorUseCase,
        UpdateCorretorUseCase,
        DeleteCorretorUseCase,
        GetCorretorUseCase,
        GetCurrentCorretorUseCase {

    private final CorretorRepositoryPort repository;
    private final CurrentUserPort currentUser;

    public CorretorUseCasesImpl(CorretorRepositoryPort repository, CurrentUserPort currentUser) {
        this.repository = repository;
        this.currentUser = currentUser;
    }

    @Override
    public Corretor create(Corretor payload) {
        // Regra de negócio: CORRETOR não pode cadastrar corretor
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            throw new AccessDeniedException("Corretores não podem cadastrar corretores.");
        }

        if (payload.usuarioId() != null && repository.existsByUsuarioId(payload.usuarioId())) {
            throw new IllegalStateException("Já existe corretor vinculado ao usuário " + payload.usuarioId());
        }
        Corretor toSave = new Corretor(
                null,
                payload.usuarioId(),
                payload.nome(),
                payload.corretora(),
                payload.cpfCnpj(),
                payload.susepPj(),
                payload.susepPf(),
                payload.email(),
                payload.telefone(),
                payload.uf(),
                payload.dataNascimento(),
                payload.doc(),
                LocalDateTime.now()
        );
        return repository.save(toSave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Corretor> listAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Corretor> findById(CorretorId id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Corretor> update(CorretorId id, Corretor updates) {
        return repository.findById(id).map(existing -> {
            Corretor updated = new Corretor(
                    existing.id(),
                    updates.usuarioId() != null ? updates.usuarioId() : existing.usuarioId(),
                    updates.nome() != null ? updates.nome() : existing.nome(),
                    updates.corretora() != null ? updates.corretora() : existing.corretora(),
                    updates.cpfCnpj() != null ? updates.cpfCnpj() : existing.cpfCnpj(),
                    updates.susepPj() != null ? updates.susepPj() : existing.susepPj(),
                    updates.susepPf() != null ? updates.susepPf() : existing.susepPf(),
                    updates.email() != null ? updates.email() : existing.email(),
                    updates.telefone() != null ? updates.telefone() : existing.telefone(),
                    updates.uf() != null ? updates.uf() : existing.uf(),
                    updates.dataNascimento() != null ? updates.dataNascimento() : existing.dataNascimento(),
                    updates.doc() != null ? updates.doc() : existing.doc(),
                    existing.dataCriacao()
            );
            return repository.save(updated);
        });
    }

    @Override
    public boolean delete(CorretorId id) {
        return repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Corretor> findCurrentCorretor() {
        Long userId = currentUser.userId();
        if (userId == null) return Optional.empty();
        return repository.findByUsuarioId(userId);
    }
}
