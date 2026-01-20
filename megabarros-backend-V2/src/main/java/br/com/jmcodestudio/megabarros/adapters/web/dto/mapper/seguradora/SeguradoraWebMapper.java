package br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.seguradora;

import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraUpdateRequest;
import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeguradoraWebMapper {

    // Criação: usa nomeSeguradora do request; produtos nunca nulos
    default Seguradora toDomain(SeguradoraCreateRequest req) {
        List<Produto> produtos = (req.produtos() == null)
                ? List.of()
                : req.produtos().stream().map(this::toDomain).toList();
        return new Seguradora(null, req.nomeSeguradora(), produtos);
    }

    // Atualização: usa nomeSeguradora; gestão de produtos é por endpoints próprios
    default Seguradora toDomain(Integer id, SeguradoraUpdateRequest req) {
        return new Seguradora(new SeguradoraId(id), req.nomeSeguradora(), List.of());
    }

    // Produto do request -> domínio
    default Produto toDomain(ProdutoRequest req) {
        return new Produto(null, null, req.nomeProduto(), req.tipoProduto());
    }

    // Responses: MapStruct gera implementação; apoliceCount preenchido no controller
    @Mapping(target = "idProduto", source = "id.value")
    @Mapping(target = "nomeProduto", source = "nome")
    @Mapping(target = "tipoProduto", source = "tipo")
    @Mapping(target = "apoliceCount", ignore = true)
    ProdutoResponse toResponse(Produto domain);

    @Mapping(target = "idSeguradora", source = "id.value")
    @Mapping(target = "nomeSeguradora", source = "nome")
    @Mapping(target = "apoliceCount", ignore = true)
    @Mapping(target = "produtos", ignore = true)
    SeguradoraResponse toResponse(Seguradora domain);
}