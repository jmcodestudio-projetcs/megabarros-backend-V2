package br.com.jmcodestudio.megabarros.application.port.in.produto;

import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;

import java.util.Optional;

public interface UpdateProdutoUseCase {
    Optional<Produto> update(ProdutoId id, Produto produto);
}
