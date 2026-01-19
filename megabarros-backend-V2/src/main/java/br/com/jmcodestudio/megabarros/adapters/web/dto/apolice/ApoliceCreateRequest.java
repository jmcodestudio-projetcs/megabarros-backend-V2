package br.com.jmcodestudio.megabarros.adapters.web.dto.apolice;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ApoliceCreateRequest(
        @NotBlank @Size(max = 50) String numeroApolice,
        @NotNull LocalDate dataEmissao,
        @NotNull LocalDate vigenciaInicio,
        @NotNull LocalDate vigenciaFim,
        @NotNull BigDecimal valor,
        @NotNull BigDecimal comissaoPercentual,
        @NotBlank @Size(max = 50) String tipoContrato,
        @NotNull Integer idCorretorCliente,
        @NotNull Integer idProduto,
        @NotNull Integer idSeguradora,
        List<CoberturaItem> coberturas,
        List<BeneficiarioItem> beneficiarios
) {
    public record CoberturaItem(@NotNull Integer idCobertura, @NotNull BigDecimal valorContratado) {}
    public record BeneficiarioItem(Integer idCliente, @NotBlank String nomeBeneficiario, String cpf, @NotNull BigDecimal percentualParticipacao) {}
}