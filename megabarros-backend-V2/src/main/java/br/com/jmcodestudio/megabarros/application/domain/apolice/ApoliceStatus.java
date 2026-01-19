package br.com.jmcodestudio.megabarros.application.domain.apolice;

import java.time.LocalDateTime;

public record ApoliceStatus(
        Integer id,
        ApoliceId apoliceId,
        String status,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {}
