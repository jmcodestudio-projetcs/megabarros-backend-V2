package br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora;

import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoResponse;

import java.util.List;

public record SeguradoraResponse(
        Integer idSeguradora,
        String nomeSeguradora,
        long apoliceCount,
        List<ProdutoResponse> produtos
) {}
