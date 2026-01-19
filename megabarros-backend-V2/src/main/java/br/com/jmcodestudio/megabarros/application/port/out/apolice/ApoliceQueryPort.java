package br.com.jmcodestudio.megabarros.application.port.out.apolice;

import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;

public interface ApoliceQueryPort {
    boolean existsBySeguradoraId(SeguradoraId seguradoraId);
    boolean existsByProdutoId(ProdutoId produtoId);

    long countBySeguradoraId(SeguradoraId seguradoraId);
    long countByProdutoId(ProdutoId produtoId);
}
