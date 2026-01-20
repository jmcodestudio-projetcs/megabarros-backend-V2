package br.com.jmcodestudio.megabarros.application.usecase.cliente;

import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;
import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.CreateClienteUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.DeactivateClienteUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.ListClientesUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.cliente.UpdateClienteUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceQueryPort;
import br.com.jmcodestudio.megabarros.application.port.out.cliente.ClienteRepositoryPort;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorClienteQueryPort;
import br.com.jmcodestudio.megabarros.application.port.out.corretor.CorretorQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteUseCasesImpl implements
        CreateClienteUseCase, UpdateClienteUseCase, DeactivateClienteUseCase, ListClientesUseCase {

    private static final Logger log = LoggerFactory.getLogger(ClienteUseCasesImpl.class);

    private final ClienteRepositoryPort repo;
    private final CurrentUserPort currentUser;
    private final CorretorQueryPort corretorQuery;
    private final CorretorClienteQueryPort corretorClienteQuery;
    private final ApoliceQueryPort apoliceQuery;

    public ClienteUseCasesImpl(ClienteRepositoryPort repo,
                               CurrentUserPort currentUser,
                               CorretorQueryPort corretorQuery,
                               CorretorClienteQueryPort corretorClienteQuery,
                               ApoliceQueryPort apoliceQuery) {
        this.repo = repo;
        this.currentUser = currentUser;
        this.corretorQuery = corretorQuery;
        this.corretorClienteQuery = corretorClienteQuery;
        this.apoliceQuery = apoliceQuery;
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
        String actor = currentUser.username();
        String role = currentUser.role();
        log.info("cliente.create start actor={} role={} cpfCnpj={}", actor, role, cliente.cpfCnpj());

        if (!isAdminOrUsuario()) {
            log.warn("cliente.create denied actor={} role={}", actor, role);
            throw new AccessDeniedException("Apenas ADMIN/USUARIO podem criar clientes.");
        }
        repo.findByCpfCnpj(cliente.cpfCnpj()).ifPresent(c -> {
            log.warn("cliente.create conflict actor={} role={} cpfCnpj={}", actor, role, cliente.cpfCnpj());
            throw new IllegalStateException("CPF/CNPJ já cadastrado.");
        });
        Cliente novo = new Cliente(null,
                cliente.nome(), cliente.cpfCnpj(), cliente.dataNascimento(),
                cliente.email(), cliente.telefone(), true, cliente.enderecos());
        Cliente saved = repo.save(novo);
        log.info("cliente.create success actor={} role={} id={}", actor, role, saved.id() != null ? saved.id().value() : null);
        return saved;
    }

    @Override
    public Optional<Cliente> update(ClienteId id, Cliente updates) {
        String actor = currentUser.username();
        String role = currentUser.role();
        log.info("cliente.update start actor={} role={} id={}", actor, role, id.value());

        if (isCorretor()) {
            Long userId = currentUser.userId();
            Integer corretorId = userId != null ? corretorQuery.findCorretorIdByUsuarioId(userId)
                    : corretorQuery.findCorretorIdByUsuarioEmail(actor);
            if (corretorId == null || !corretorClienteQuery.existsByCorretorIdAndClienteId(corretorId, id.value())) {
                log.warn("cliente.update denied not-linked actor={} role={} id={}", actor, role, id.value());
                throw new AccessDeniedException("Corretores só podem alterar clientes vinculados.");
            }
            if (updates.nome() != null || updates.cpfCnpj() != null || updates.dataNascimento() != null) {
                log.warn("cliente.update denied invalid-fields actor={} role={} id={}", actor, role, id.value());
                throw new AccessDeniedException("Corretores só podem alterar email e telefone.");
            }
            Optional<Cliente> res = repo.findById(id).map(existing -> {
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
            log.info("cliente.update success actor={} role={} id={} updated={}", actor, role, id.value(), res.isPresent());
            return res;
        }

        if (!isAdminOrUsuario()) {
            log.warn("cliente.update denied actor={} role={} id={}", actor, role, id.value());
            throw new AccessDeniedException("Perfil não autorizado.");
        }

        Optional<Cliente> res = repo.findById(id).map(existing -> {
            String novoCpf = updates.cpfCnpj() != null ? updates.cpfCnpj() : existing.cpfCnpj();
            if (!novoCpf.equals(existing.cpfCnpj())) {
                repo.findByCpfCnpj(novoCpf).ifPresent(c -> {
                    log.warn("cliente.update conflict actor={} role={} id={} cpfCnpj={}", actor, role, id.value(), novoCpf);
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
        log.info("cliente.update success actor={} role={} id={} updated={}", actor, role, id.value(), res.isPresent());
        return res;
    }


    @Override
    public void deactivate(ClienteId id) {
        String actor = currentUser.username();
        String role = currentUser.role();
        log.info("cliente.deactivate start actor={} role={} id={}", actor, role, id.value());

        if (!isAdminOrUsuario()) {
            log.warn("cliente.deactivate denied actor={} role={} id={}", actor, role, id.value());
            throw new AccessDeniedException("Apenas ADMIN/USUARIO podem desativar clientes.");
        }
        if (apoliceQuery.existsActiveByClienteId(id.value())) {
            log.warn("cliente.deactivate conflict active-apolice actor={} role={} id={}", actor, role, id.value());
            throw new IllegalStateException("Não é possível desativar: cliente possui apólice ativa.");
        }
        repo.deactivate(id);
        log.info("cliente.deactivate success actor={} role={} id={}", actor, role, id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listAll() {
        String actor = currentUser.username();
        String role = currentUser.role();
        List<Cliente> res = isCorretor() ? listMine() : repo.findAll();
        log.info("cliente.listAll actor={} role={} count={}", actor, role, res.size());
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listMine() {
        String actor = currentUser.username();
        Integer corretorId = corretorQuery.findCorretorIdByUsuarioId(currentUser.userId());
        if (corretorId == null) corretorId = corretorQuery.findCorretorIdByUsuarioEmail(actor);
        List<Cliente> res = (corretorId == null) ? List.of() : repo.findByCorretorId(corretorId);
        log.info("cliente.listMine actor={} corretorId={} count={}", actor, corretorId, res.size());
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> getById(Integer id) {
        String actor = currentUser.username();
        String role = currentUser.role();
        if (isCorretor()) {
            Integer corretorId = corretorQuery.findCorretorIdByUsuarioId(currentUser.userId());
            if (corretorId == null) corretorId = corretorQuery.findCorretorIdByUsuarioEmail(actor);
            if (corretorId == null || !corretorClienteQuery.existsByCorretorIdAndClienteId(corretorId, id)) {
                log.warn("cliente.getById denied not-linked actor={} role={} id={}", actor, role, id);
                throw new AccessDeniedException("Corretores só podem consultar clientes vinculados.");
            }
        }
        Optional<Cliente> res = repo.findById(new ClienteId(id));
        log.info("cliente.getById actor={} role={} id={} found={}", actor, role, id, res.isPresent());
        return res;
    }

}
