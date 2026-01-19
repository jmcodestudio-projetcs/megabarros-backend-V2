package br.com.jmcodestudio.megabarros.application.domain.parcela;

import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Parcela(
        Integer id,
        ApoliceId apoliceId,
        Integer numeroParcela,
        LocalDate dataVencimento,
        BigDecimal valorParcela,
        String statusPagamento,
        LocalDate dataPagamento
) {}
