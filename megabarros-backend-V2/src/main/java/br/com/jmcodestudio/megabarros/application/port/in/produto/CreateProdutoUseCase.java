package br.com.jmcodestudio.megabarros.application.port.in.produto;

import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;

public interface CreateProdutoUseCase {
    Produto create(Produto produto);
}
