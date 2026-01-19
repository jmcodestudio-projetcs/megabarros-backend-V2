package br.com.jmcodestudio.megabarros.adapters.web.dto.apolice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ApoliceResponse(
        Integer idApolice,
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
        List<ParcelaResponse> parcelas,
        List<CoberturaResponse> coberturas,
        List<BeneficiarioResponse> beneficiarios
) {
    public record ParcelaResponse(Integer idParcela, Integer numeroParcela, LocalDate dataVencimento, BigDecimal valorParcela, String statusPagamento, LocalDate dataPagamento) {}
    public record CoberturaResponse(Integer idApoliceCobertura, Integer idCobertura, BigDecimal valorContratado) {}
    public record BeneficiarioResponse(Integer idBeneficiario, Integer idCliente, String nomeBeneficiario, String cpf, BigDecimal percentualParticipacao) {}
}