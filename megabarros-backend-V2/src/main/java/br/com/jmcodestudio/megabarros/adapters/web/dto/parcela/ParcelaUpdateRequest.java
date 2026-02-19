package br.com.jmcodestudio.megabarros.adapters.web.dto.parcela;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaUpdateRequest(
        Integer idParcela,
        LocalDate dataVencimento,
        BigDecimal valorParcela,
        Boolean remover
) {}
