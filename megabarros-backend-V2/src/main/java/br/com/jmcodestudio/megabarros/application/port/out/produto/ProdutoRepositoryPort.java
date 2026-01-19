package br.com.jmcodestudio.megabarros.application.port.out.produto;

import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepositoryPort {
    Produto save(Produto produto);
    List<Produto> findBySeguradoraId(SeguradoraId seguradoraId);
    Optional<Produto> findById(ProdutoId id);
    void deleteById(ProdutoId id);
}
