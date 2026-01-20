package br.com.jmcodestudio.megabarros.application.usecase.seguradora;

import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import br.com.jmcodestudio.megabarros.application.port.in.seguradora.CreateSeguradoraUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.seguradora.DeleteSeguradoraUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.seguradora.ListSeguradorasUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.seguradora.UpdateSeguradoraUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceQueryPort;
import br.com.jmcodestudio.megabarros.application.port.out.produto.ProdutoRepositoryPort;
import br.com.jmcodestudio.megabarros.application.port.out.seguradora.SeguradoraRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SeguradoraUseCasesImpl implements
        CreateSeguradoraUseCase, UpdateSeguradoraUseCase, DeleteSeguradoraUseCase, ListSeguradorasUseCase {

    private static final Logger log = LoggerFactory.getLogger(SeguradoraUseCasesImpl.class);

    private final SeguradoraRepositoryPort seguradoraRepo;
    private final ProdutoRepositoryPort produtoRepo;
    private final ApoliceQueryPort apoliceQuery;
    private final CurrentUserPort currentUser;

    public SeguradoraUseCasesImpl(SeguradoraRepositoryPort seguradoraRepo,
                                  ProdutoRepositoryPort produtoRepo,
                                  ApoliceQueryPort apoliceQuery,
                                  CurrentUserPort currentUser) {
        this.seguradoraRepo = seguradoraRepo;
        this.produtoRepo = produtoRepo;
        this.apoliceQuery = apoliceQuery;
        this.currentUser = currentUser;
    }

    @Override
    public Seguradora create(Seguradora seguradora) {
        String actor = currentUser.username();
        String role = currentUser.role();
        log.info("seguradora.create start actor={} role={} nome={}", actor, role, seguradora.nome());

        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            log.warn("seguradora.create denied actor={} role={}", actor, role);
            throw new AccessDeniedException("Corretores não podem cadastrar seguradoras.");
        }

        Seguradora created = seguradoraRepo.save(new Seguradora(null, seguradora.nome(), List.of()));

        List<Produto> produtosCriados = List.of();
        if (seguradora.produtos() != null) {
            produtosCriados = seguradora.produtos().stream()
                    .map(p -> produtoRepo.save(new Produto(null, SeguradoraId.of(created.id().value()), p.nome(), p.tipo())))
                    .toList();
        }
        Seguradora res = new Seguradora(created.id(), created.nome(), produtosCriados);
        log.info("seguradora.create success actor={} id={} produtosCount={}", actor, res.id().value(), res.produtos().size());
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seguradora> listAll() {
        var list = seguradoraRepo.findAll().stream()
                .map(s -> new Seguradora(s.id(), s.nome(), produtoRepo.findBySeguradoraId(s.id())))
                .toList();
        log.info("seguradora.listAll actor={} role={} count={}", currentUser.username(), currentUser.role(), list.size());
        return list;
    }


    @Override
    public Optional<Seguradora> update(SeguradoraId id, Seguradora updates) {
        String actor = currentUser.username();
        String role = currentUser.role();
        log.info("seguradora.update start actor={} role={} id={}", actor, role, id.value());

        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            log.warn("seguradora.update denied actor={} role={}", actor, role);
            throw new AccessDeniedException("Corretores não podem atualizar seguradoras.");
        }

        return seguradoraRepo.findById(id).map(existing -> {
            Seguradora saved = seguradoraRepo.save(new Seguradora(existing.id(),
                    updates.nome() != null ? updates.nome() : existing.nome(),
                    existing.produtos()));
            List<Produto> produtos = produtoRepo.findBySeguradoraId(saved.id());
            Seguradora res = new Seguradora(saved.id(), saved.nome(), produtos);
            log.info("seguradora.update success actor={} id={}", actor, id.value());
            return res;
        });
    }

    @Override
    public void delete(SeguradoraId id) {
        String actor = currentUser.username();
        log.info("seguradora.delete start actor={} id={}", actor, id.value());

        if (currentUser.role() != null && currentUser.role().equalsIgnoreCase("CORRETOR")) {
            log.warn("seguradora.delete denied actor={} role={}", actor, currentUser.role());
            throw new AccessDeniedException("Corretores não podem excluir seguradoras.");
        }

        if (apoliceQuery.existsBySeguradoraId(id)) {
            log.warn("seguradora.delete conflict apolices-exist actor={} id={}", actor, id.value());
            throw new IllegalStateException("Não é possível excluir a seguradora: existem apólices vinculadas.");
        }
        List<Produto> produtos = produtoRepo.findBySeguradoraId(id);
        for (Produto p : produtos) {
            if (apoliceQuery.existsByProdutoId(p.id())) {
                log.warn("seguradora.delete conflict apolices-by-prod actor={} produtoId={}", actor, p.id().value());
                throw new IllegalStateException("Não é possível excluir a seguradora: apólices vinculadas a produtos.");
            }
            produtoRepo.deleteById(p.id());
        }
        seguradoraRepo.deleteById(id);
        log.info("seguradora.delete success actor={} id={}", actor, id.value());
    }
}