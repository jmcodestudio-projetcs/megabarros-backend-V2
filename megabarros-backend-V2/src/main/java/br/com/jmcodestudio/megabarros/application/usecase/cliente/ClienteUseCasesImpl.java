package br.com.jmcodestudio.megabarros.application.usecase.cliente;

import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;
import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.*;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.cliente.ClienteRepositoryPort;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorClienteQueryPort;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorQueryPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteUseCasesImpl implements
        CreateClienteUseCase, UpdateClienteUseCase, DeactivateClienteUseCase, ListClientesUseCase {

    private final ClienteRepositoryPort repo;
    private final CurrentUserPort currentUser;
    private final CorretorQueryPort corretorQuery;
    private final CorretorClienteQueryPort corretorClienteQuery;

    public ClienteUseCasesImpl(ClienteRepositoryPort repo,
                               CurrentUserPort currentUser,
                               CorretorQueryPort corretorQuery,
                               CorretorClienteQueryPort corretorClienteQuery) {
        this.repo = repo;
        this.currentUser = currentUser;
        this.corretorQuery = corretorQuery;
        this.corretorClienteQuery = corretorClienteQuery;
    }

    private boolean isCorretor() {
        String role = currentUser.role();
        return role != null && role.equalsIgnoreCase("CORRETOR");
    }

    private boolean isAdminOrUsuario() {
        String role = currentUser.role();
        return role != null && (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("USUARIO"));
    }

    @Override
    public Cliente create(Cliente cliente) {
        if (!isAdminOrUsuario()) {
            throw new AccessDeniedException("Apenas ADMIN/USUARIO podem criar clientes.");
        }
        // unicidade cpfCnpj
        repo.findByCpfCnpj(cliente.cpfCnpj()).ifPresent(c -> {
            throw new IllegalStateException("CPF/CNPJ já cadastrado.");
        });
        Cliente novo = new Cliente(null,
                cliente.nome(), cliente.cpfCnpj(), cliente.dataNascimento(),
                cliente.email(), cliente.telefone(), true, cliente.enderecos());
        return repo.save(novo);
    }

    @Override
    public Optional<Cliente> update(ClienteId id, Cliente updates) {
        if (isCorretor()) {
            Long userId = currentUser.userId();
            Integer corretorId = null;
            if (userId != null) {
                corretorId = corretorQuery.findCorretorIdByUsuarioId(userId);
            }
            if (corretorId == null) {
                String email = currentUser.username();
                if (email != null) {
                    corretorId = corretorQuery.findCorretorIdByUsuarioEmail(email);
                }
            }
            if (corretorId == null || !corretorClienteQuery.existsByCorretorIdAndClienteId(corretorId, id.value())) {
                throw new AccessDeniedException("Corretores só podem alterar clientes vinculados.");
            }
            if (updates.nome() != null || updates.cpfCnpj() != null || updates.dataNascimento() != null) {
                throw new AccessDeniedException("Corretores só podem alterar email e telefone.");
            }
            return repo.findById(id).map(existing -> {
                Cliente merged = new Cliente(
                        existing.id(),
                        existing.nome(),
                        existing.cpfCnpj(),
                        existing.dataNascimento(),
                        updates.email() != null ? updates.email() : existing.email(),
                        updates.telefone() != null ? updates.telefone() : existing.telefone(),
                        existing.ativo(),
                        existing.enderecos()
                );
                return repo.save(merged);
            });
        }

        if (!isAdminOrUsuario()) {
            throw new AccessDeniedException("Perfil não autorizado.");
        }

        // ADMIN/USUARIO: pode alterar todos os campos. Garantir unicidade CPF/CNPJ se mudou.
        return repo.findById(id).map(existing -> {
            String novoCpf = updates.cpfCnpj() != null ? updates.cpfCnpj() : existing.cpfCnpj();
            if (!novoCpf.equals(existing.cpfCnpj())) {
                repo.findByCpfCnpj(novoCpf).ifPresent(c -> {
                    throw new IllegalStateException("CPF/CNPJ já cadastrado.");
                });
            }
            Cliente merged = new Cliente(
                    existing.id(),
                    updates.nome() != null ? updates.nome() : existing.nome(),
                    novoCpf,
                    updates.dataNascimento() != null ? updates.dataNascimento() : existing.dataNascimento(),
                    updates.email() != null ? updates.email() : existing.email(),
                    updates.telefone() != null ? updates.telefone() : existing.telefone(),
                    existing.ativo(),
                    existing.enderecos()
            );
            return repo.save(merged);
        });
    }

    @Override
    public void deactivate(ClienteId id) {
        if (!isAdminOrUsuario()) {
            throw new AccessDeniedException("Apenas ADMIN/USUARIO podem desativar clientes.");
        }
        repo.deactivate(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listAll() {
        if (isCorretor()) {
            return listMine();
        }
        return repo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listMine() {
        if (!isCorretor()) return repo.findAll();
        Integer corretorId = corretorQuery.findCorretorIdByUsuarioId(currentUser.userId());
        if (corretorId == null) return List.of();
        return repo.findByCorretorId(corretorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> getById(Integer id) {
        if (isCorretor()) {
            Integer corretorId = corretorQuery.findCorretorIdByUsuarioId(currentUser.userId());
            if (corretorId == null || !corretorClienteQuery.existsByCorretorIdAndClienteId(corretorId, id)) {
                throw new AccessDeniedException("Corretores só podem consultar clientes vinculados.");
            }
        }
        return repo.findById(new ClienteId(id));
    }
}
