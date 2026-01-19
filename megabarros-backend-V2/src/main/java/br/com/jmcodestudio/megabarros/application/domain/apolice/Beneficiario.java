package br.com.jmcodestudio.megabarros.application.domain.apolice;

import java.math.BigDecimal;

public record Beneficiario(
        Integer id,
        ApoliceId apoliceId,
        Integer idCliente,
        String nomeBeneficiario,
        String cpf,
        BigDecimal percentualParticipacao
) {}
