package br.com.jmcodestudio.megabarros.adapters.web.dto.apolice;

import br.com.jmcodestudio.megabarros.adapters.web.dto.parcela.ParcelaUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        Integer idSeguradora,
        List<ParcelaUpdateRequest> parcelas
) {
}
