package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice.ApoliceExistenceRepository;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import br.com.jmcodestudio.megabarros.application.port.out.apolice.ApoliceQueryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class ApoliceQueryAdapter implements ApoliceQueryPort {

    private final ApoliceExistenceRepository repo;

    public ApoliceQueryAdapter(ApoliceExistenceRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean existsBySeguradoraId(SeguradoraId seguradoraId) {
        return repo.existsBySeguradoraId(seguradoraId.value());
    }

    @Override
    public boolean existsByProdutoId(ProdutoId produtoId) {
        return repo.existsByProdutoId(produtoId.value());
    }

    @Override
    public long countBySeguradoraId(SeguradoraId seguradoraId) {
        return repo.countBySeguradoraId(seguradoraId.value());
    }

    @Override
    public long countByProdutoId(ProdutoId produtoId) {
        return repo.countByProdutoId(produtoId.value());
    }
}
