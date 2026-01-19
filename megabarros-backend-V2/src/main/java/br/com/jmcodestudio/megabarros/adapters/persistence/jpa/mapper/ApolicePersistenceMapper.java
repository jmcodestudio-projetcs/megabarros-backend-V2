package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceStatusEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.parcela.ParcelaApoliceEntity;
import br.com.jmcodestudio.megabarros.application.domain.apolice.Apolice;
import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;
import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceStatus;
import br.com.jmcodestudio.megabarros.application.domain.parcela.Parcela;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApolicePersistenceMapper {

    // --------- Apólice ---------
    default Apolice toDomain(ApoliceEntity e,
                             String statusAtual,
                             List<Parcela> parcelas,
                             List<?> coberturasVazias,
                             List<?> beneficiariosVazios) {
        if (e == null) return null;
        return new Apolice(
                ApoliceId.of(e.getId()),
                e.getNumero(),
                e.getDataEmissao(),
                e.getVigenciaInicio(),
                e.getVigenciaFim(),
                e.getValor(),
                e.getComissaoPercentual(),
                e.getTipoContrato(),
                e.getIdCorretorCliente(),
                e.getIdProduto(),
                e.getIdSeguradora(),
                statusAtual,
                parcelas,
                List.of(),        // coberturas vazias nesta fase
                List.of()         // beneficiários vazios nesta fase
        );
    }

    default ApoliceEntity toEntity(Apolice d) {
        if (d == null) return null;
        ApoliceEntity e = new ApoliceEntity();
        e.setId(d.id() != null ? d.id().value() : null);
        e.setNumero(d.numeroApolice());
        e.setDataEmissao(d.dataEmissao());
        e.setVigenciaInicio(d.vigenciaInicio());
        e.setVigenciaFim(d.vigenciaFim());
        e.setValor(d.valor());
        e.setComissaoPercentual(d.comissaoPercentual());
        e.setTipoContrato(d.tipoContrato());
        e.setIdCorretorCliente(d.idCorretorCliente());
        e.setIdProduto(d.idProduto());
        e.setIdSeguradora(d.idSeguradora());
        return e;
    }

    // --------- Parcela ---------
    default Parcela toDomain(ParcelaApoliceEntity e) {
        if (e == null) return null;
        return new Parcela(
                e.getId(),
                ApoliceId.of(e.getIdApolice()),
                e.getNumeroParcela(),
                e.getDataVencimento(),
                e.getValorParcela(),
                e.getStatusPagamento(),
                e.getDataPagamento()
        );
    }

    default ParcelaApoliceEntity toEntity(Parcela d) {
        if (d == null) return null;
        ParcelaApoliceEntity e = new ParcelaApoliceEntity();
        e.setId(d.id());
        e.setIdApolice(d.apoliceId() != null ? d.apoliceId().value() : null);
        e.setNumeroParcela(d.numeroParcela());
        e.setDataVencimento(d.dataVencimento());
        e.setValorParcela(d.valorParcela());
        e.setStatusPagamento(d.statusPagamento());
        e.setDataPagamento(d.dataPagamento());
        return e;
    }

    // --------- Status ---------
    default ApoliceStatus toDomain(ApoliceStatusEntity e) {
        if (e == null) return null;
        return new ApoliceStatus(
                e.getId(),
                ApoliceId.of(e.getIdApolice()),
                e.getStatus(),
                e.getDataInicio(),
                e.getDataFim()
        );
    }

    default ApoliceStatusEntity toEntity(ApoliceStatus d) {
        if (d == null) return null;
        ApoliceStatusEntity e = new ApoliceStatusEntity();
        e.setId(d.id());
        e.setIdApolice(d.apoliceId() != null ? d.apoliceId().value() : null);
        e.setStatus(d.status());
        e.setDataInicio(d.dataInicio());
        e.setDataFim(d.dataFim());
        return e;
    }
}