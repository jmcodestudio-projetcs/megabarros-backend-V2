package br.com.jmcodestudio.megabarros.application.port.out.apolice;

import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;

import java.util.List;
import java.util.Map;

public interface ApoliceQueryPort {
    boolean existsBySeguradoraId(SeguradoraId seguradoraId);
    boolean existsByProdutoId(ProdutoId produtoId);

    long countBySeguradoraId(SeguradoraId seguradoraId);
    long countByProdutoId(ProdutoId produtoId);
    // Novo: existe apólice ATIVA para o cliente?
    boolean existsActiveByClienteId(Integer clienteId);
    // Novo: agregações
    Map<Integer, Long> countBySeguradoraIds(List<SeguradoraId> ids);
    Map<Integer, Long> countByProdutoIds(List<ProdutoId> ids);
}
