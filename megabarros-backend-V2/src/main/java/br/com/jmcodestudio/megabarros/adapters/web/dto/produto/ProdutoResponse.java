package br.com.jmcodestudio.megabarros.adapters.web.dto.produto;

public record ProdutoResponse(
        Integer idProduto,
        String nomeProduto,
        String tipoProduto,
        long apoliceCount
) {}
