package br.com.jmcodestudio.megabarros.application.usecase.produto;

import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import br.com.jmcodestudio.megabarros.application.port.in.produto.CreateProdutoUseCase;
import br.com.jmcodestudio.megabarros.application.port.in.produto.DeleteProdutoUseCase;
import br.com.jmcodestudio.megabarros.application.port.out.CurrentUserPort;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceQueryPort;
import br.com.jmcodestudio.megabarros.application.port.out.produto.ProdutoRepositoryPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProdutoUseCasesImpl implements CreateProdutoUseCase, DeleteProdutoUseCase {

    private final ProdutoRepositoryPort produtoRepo;
    private final ApoliceQueryPort apoliceQuery;
    private final CurrentUserPort currentUser;

    public ProdutoUseCasesImpl(ProdutoRepositoryPort produtoRepo, ApoliceQueryPort apoliceQuery, CurrentUserPort currentUser) {
        this.produtoRepo = produtoRepo;
        this.apoliceQuery = apoliceQuery;
        this.currentUser = currentUser;
    }

    @Override
    public Produto create(Produto produto) {
        // Regra: CORRETOR não pode cadastrar produto
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            throw new AccessDeniedException("Corretores não podem cadastrar produtos.");
        }
        return produtoRepo.save(new Produto(null, SeguradoraId.of(produto.seguradoraId().value()),
                produto.nome(), produto.tipo()));
    }

    @Override
    public void delete(ProdutoId id) {
        // Regra: CORRETOR não pode excluir produto
        String role = currentUser.role();
        if (role != null && role.equalsIgnoreCase("CORRETOR")) {
            throw new AccessDeniedException("Corretores não podem excluir produtos.");
        }
        if (apoliceQuery.existsByProdutoId(id)) {
            throw new IllegalStateException("Não é possível excluir o produto: existem apólices vinculadas.");
        }
        produtoRepo.deleteById(id);
    }
}
