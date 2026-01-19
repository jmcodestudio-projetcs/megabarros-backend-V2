package br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora;

import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SeguradoraCreateRequest(
        @NotBlank @Size(max = 100) String nomeSeguradora,
        List<ProdutoRequest> produtos
) {}
