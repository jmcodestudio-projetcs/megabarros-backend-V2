package br.com.jmcodestudio.megabarros.adapters.web.dto.apolice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApoliceUpdateRequest(
        String numeroApolice,
        LocalDate dataEmissao,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFim,
        BigDecimal valor,
        BigDecimal comissaoPercentual,
        String tipoContrato,
        Integer idCorretorCliente,
        Integer idProduto,
        Integer idSeguradora
) {}
