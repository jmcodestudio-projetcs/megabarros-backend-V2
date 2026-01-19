package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.produto.ProdutoEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.seguradora.SeguradoraEntity;
import br.com.jmcodestudio.megabarros.application.domain.produto.Produto;
import br.com.jmcodestudio.megabarros.application.domain.produto.ProdutoId;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProdutoPersistenceMapper {

    @Mapping(source = "id", target = "id", qualifiedByName = "toProdId")
    @Mapping(source = "seguradora.id", target = "seguradoraId", qualifiedByName = "toSegId")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "tipo", target = "tipo")
    Produto toDomain(ProdutoEntity entity);

    @Mapping(source = "id.value", target = "id")
    @Mapping(target = "seguradora", source = "seguradoraId", qualifiedByName = "toSegEntity")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "tipo", target = "tipo")
    ProdutoEntity toEntity(Produto domain);

    @Named("toProdId")
    default ProdutoId toProdId(Integer id) { return id == null ? null : new ProdutoId(id); }

    @Named("toSegId")
    default SeguradoraId toSegId(Integer id) { return id == null ? null : new SeguradoraId(id); }

    @Named("toSegEntity")
    default SeguradoraEntity toSegEntity(SeguradoraId id) {
        if (id == null) return null;
        SeguradoraEntity s = new SeguradoraEntity();
        s.setId(id.value());
        return s;
    }
}
