package br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.seguradora;

import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.produto.ProdutoResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.seguradora.SeguradoraUpdateRequest;
import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeguradoraWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nome", source = "nomeSeguradora")
    @Mapping(target = "produtos", source = "produtos")
    Seguradora toDomain(SeguradoraCreateRequest req);

    @Mapping(target = "id", expression = "java(new SeguradoraId(id))")
    @Mapping(target = "nome", source = "req.nomeSeguradora")
    @Mapping(target = "produtos", ignore = true)
    Seguradora toDomain(Integer id, SeguradoraUpdateRequest req);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seguradoraId", ignore = true)
    @Mapping(target = "nome", source = "nomeProduto")
    @Mapping(target = "tipo", source = "tipoProduto")
    Produto toDomain(ProdutoRequest req);

    // Para responses com contagem, o controller monta manualmente usando helper
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

    default List<Produto> toDomainProdutos(List<ProdutoRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream().map(this::toDomain).toList();
    }

    @Named("toSegId")
    default SeguradoraId toSegId(Integer id) { return id == null ? null : new SeguradoraId(id); }

    @Named("toProdId")
    default ProdutoId toProdId(Integer id) { return id == null ? null : new ProdutoId(id); }
}
