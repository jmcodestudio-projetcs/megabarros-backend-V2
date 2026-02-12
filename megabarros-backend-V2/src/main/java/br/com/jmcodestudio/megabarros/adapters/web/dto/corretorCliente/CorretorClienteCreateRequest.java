package br.com.jmcodestudio.megabarros.adapters.web.dto.corretorCliente;

import jakarta.validation.constraints.NotNull;

public record CorretorClienteCreateRequest(
        @NotNull Integer corretorId,
        @NotNull Integer clienteId
) {}
