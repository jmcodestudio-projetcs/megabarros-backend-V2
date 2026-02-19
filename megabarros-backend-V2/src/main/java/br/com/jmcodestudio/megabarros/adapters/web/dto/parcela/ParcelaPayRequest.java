package br.com.jmcodestudio.megabarros.adapters.web.dto.parcela;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ParcelaPayRequest(
        @NotNull LocalDate dataPagamento
) {}