package br.com.jmcodestudio.megabarros.application.domain.seguradora;

import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;

import java.util.List;

public record Seguradora(
        SeguradoraId id,
        String nome,
        List<Produto> produtos
) {}
