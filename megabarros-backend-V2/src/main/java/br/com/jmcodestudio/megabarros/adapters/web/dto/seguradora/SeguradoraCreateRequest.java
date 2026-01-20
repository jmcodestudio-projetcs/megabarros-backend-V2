package br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora;

import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SeguradoraCreateRequest(
        @JsonProperty("nome") @NotBlank String nomeSeguradora,
        @Valid List<ProdutoRequest> produtos
) {}
