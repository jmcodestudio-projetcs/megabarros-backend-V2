package br.com.jmcodestudio.megabarros.adapters.web.dto.apolice;

import br.com.jmcodestudio.megabarros.adapters.web.dto.parcela.ParcelaRequest;
import br.com.jmcodestudio.megabarros.application.domain.apolice.Apolice;
import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;
import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApoliceWebMapper {

    // Criação: ignorar coberturas/beneficiários nesta fase
    default Apolice toDomain(ApoliceCreateRequest req) {
        if (req == null) return null;
        return new Apolice(
                null,
                req.numeroApolice(),
                req.dataEmissao(),
                req.vigenciaInicio(),
                req.vigenciaFim(),
                req.valor(),
                req.comissaoPercentual(),
                req.tipoContrato(),
                req.idCorretorCliente(),
                req.idProduto(),
                req.idSeguradora(),
                null,
                List.of(), // parcelas são gerenciadas por endpoint próprio
                List.of(), // coberturas não usadas nesta fase
                List.of()  // beneficiários não usados nesta fase
        );
    }

    default Apolice toDomain(Integer id, ApoliceUpdateRequest req) {
        if (req == null) return null;
        return new Apolice(
                new ApoliceId(id),
                req.numeroApolice(),
                req.dataEmissao(),
                req.vigenciaInicio(),
                req.vigenciaFim(),
                req.valor(),
                req.comissaoPercentual(),
                req.tipoContrato(),
                req.idCorretorCliente(),
                req.idProduto(),
                req.idSeguradora(),
                null,
                List.of(),
                List.of(),
                List.of()
        );
    }

    default ApoliceResponse toResponse(Apolice d) {
        if (d == null) return null;
        return new ApoliceResponse(
                d.id() != null ? d.id().value() : null,
                d.numeroApolice(),
                d.dataEmissao(),
                d.vigenciaInicio(),
                d.vigenciaFim(),
                d.valor(),
                d.comissaoPercentual(),
                d.tipoContrato(),
                d.idCorretorCliente(),
                d.idProduto(),
                d.idSeguradora(),
                d.statusAtual(),
                // parcelas
                d.parcelas() == null ? List.of() : d.parcelas().stream()
                        .map(p -> new ApoliceResponse.ParcelaResponse(
                                p.id(), p.numeroParcela(), p.dataVencimento(), p.valorParcela(),
                                p.statusPagamento(), p.dataPagamento()
                        )).toList(),
                // coberturas: lista vazia nesta fase
                List.of(),
                // beneficiários: lista vazia nesta fase
                List.of()
        );
    }

    // Builder de parcela para endpoint dedicado
    default Parcela toParcela(Integer apoliceId, ParcelaRequest req) {
        return new Parcela(
                null,
                new ApoliceId(apoliceId),
                req.numeroParcela(),
                req.dataVencimento(),
                req.valorParcela(),
                "PENDENTE",
                null
        );
    }
}