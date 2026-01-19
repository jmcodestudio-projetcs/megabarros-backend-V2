package br.com.jmcodestudio.megabarros.application.domain.apolice;

import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record Apolice(
        ApoliceId id,
        String numeroApolice,
        LocalDate dataEmissao,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFim,
        BigDecimal valor,
        BigDecimal comissaoPercentual,
        String tipoContrato,
        Integer idCorretorCliente,
        Integer idProduto,
        Integer idSeguradora,
        String statusAtual,
        List<Parcela> parcelas,
        List<ApoliceCobertura> coberturas,
        List<Beneficiario> beneficiarios
) {}