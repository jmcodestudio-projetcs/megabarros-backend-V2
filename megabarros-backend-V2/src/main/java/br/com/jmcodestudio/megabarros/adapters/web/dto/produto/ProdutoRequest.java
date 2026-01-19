package br.com.jmcodestudio.megabarros.adapters.web.dto.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(
        @NotBlank @Size(max = 100) String nomeProduto,
        @Size(max = 50) String tipoProduto
) {}
