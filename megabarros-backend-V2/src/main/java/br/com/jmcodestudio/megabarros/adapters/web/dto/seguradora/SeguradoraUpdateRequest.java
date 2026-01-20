package br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record SeguradoraUpdateRequest(
        @JsonProperty("nome") @NotBlank String nomeSeguradora
) {}
