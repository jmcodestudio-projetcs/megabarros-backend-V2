package br.com.jmcodestudio.megabarros.application.port.in.produto;

import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;

public interface DeleteProdutoUseCase {
    void delete(ProdutoId id);
}
