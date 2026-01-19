package br.com.jmcodestudio.megabarros.adapters.web.dto.parcela;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaRequest(
        @NotNull Integer numeroParcela,
        @NotNull LocalDate dataVencimento,
        @NotNull BigDecimal valorParcela
) {}
