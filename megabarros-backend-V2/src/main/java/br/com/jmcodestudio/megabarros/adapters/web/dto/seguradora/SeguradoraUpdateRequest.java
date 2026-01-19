package br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora;

import jakarta.validation.constraints.Size;

public record SeguradoraUpdateRequest(
        @Size(max = 100) String nomeSeguradora
) {}
