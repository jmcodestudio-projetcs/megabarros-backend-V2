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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SeguradoraUseCasesImpl implements
        CreateSeguradoraUseCase, UpdateSeguradoraUseCase, DeleteSeguradoraUseCase, ListSeguradorasUseCase {

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
        // Regra: CORRETOR não pode cadastrar seguradora
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            throw new AccessDeniedException("Corretores não podem cadastrar seguradoras.");
        }

        // cria seguradora
        Seguradora created = seguradoraRepo.save(new Seguradora(null, seguradora.nome(), List.of()));

        // cria produtos vinculados
        if (seguradora.produtos() != null) {
            for (Produto p : seguradora.produtos()) {
                Produto novo = new Produto(null, SeguradoraId.of(created.id().value()), p.nome(), p.tipo());
                try {
                    produtoRepo.save(novo);
                } catch (DataIntegrityViolationException e) {
                    // quebra de unicidade (id_seguradora, nome_produto)
                    throw e;
                }
            }
        }
        // retorna com lista atualizada
        List<Produto> produtos = produtoRepo.findBySeguradoraId(created.id());
        return new Seguradora(created.id(), created.nome(), produtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seguradora> listAll() {
        return seguradoraRepo.findAll().stream()
                .map(s -> new Seguradora(s.id(), s.nome(), produtoRepo.findBySeguradoraId(s.id())))
                .toList();
    }

    @Override
    public Optional<Seguradora> update(SeguradoraId id, Seguradora updates) {
        // Regra: CORRETOR não pode atualizar seguradora
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            throw new AccessDeniedException("Corretores não podem atualizar seguradoras.");
        }

        return seguradoraRepo.findById(id).map(existing -> {
            Seguradora saved = seguradoraRepo.save(new Seguradora(existing.id(),
                    updates.nome() != null ? updates.nome() : existing.nome(),
                    existing.produtos()));
            List<Produto> produtos = produtoRepo.findBySeguradoraId(saved.id());
            return new Seguradora(saved.id(), saved.nome(), produtos);
        });
    }

    @Override
    public void delete(SeguradoraId id) {
        // Regra: CORRETOR não pode excluir seguradora
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            throw new AccessDeniedException("Corretores não podem excluir seguradoras.");
        }

        // bloqueia exclusão se há apólices vinculadas
        if (apoliceQuery.existsBySeguradoraId(id)) {
            throw new IllegalStateException("Não é possível excluir a seguradora: existem apólices vinculadas.");
        }
        // excluir produtos vinculados
        List<Produto> produtos = produtoRepo.findBySeguradoraId(id);
        for (Produto p : produtos) {
            if (apoliceQuery.existsByProdutoId(p.id())) {
                throw new IllegalStateException("Não é possível excluir a seguradora: existem apólices vinculadas a produtos desta seguradora.");
            }
            produtoRepo.deleteById(p.id());
        }
        seguradoraRepo.deleteById(id);
    }
}