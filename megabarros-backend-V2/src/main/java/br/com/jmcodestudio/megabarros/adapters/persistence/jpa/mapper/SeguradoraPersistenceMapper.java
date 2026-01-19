package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.seguradora.SeguradoraEntity;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeguradoraPersistenceMapper {

    @Mapping(source = "id", target = "id", qualifiedByName = "toId")
    Seguradora toDomain(SeguradoraEntity entity);

    @Mapping(source = "id.value", target = "id")
    @Mapping(target = "nome", source = "nome")
    SeguradoraEntity toEntity(Seguradora domain);

    @Named("toId")
    default SeguradoraId toId(Integer id) {
        return id == null ? null : new SeguradoraId(id);
    }
}
