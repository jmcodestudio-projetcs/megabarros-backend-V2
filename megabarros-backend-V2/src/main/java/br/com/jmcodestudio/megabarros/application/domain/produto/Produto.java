package br.com.jmcodestudio.megabarros.application.domain.produto;

import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;

public record Produto(
        ProdutoId id,
        SeguradoraId seguradoraId,
        String nome,
        String tipo
) {}
