package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.corretor.CorretorEntity;
import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;
import br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CorretorPersistenceMapper {

    // Entity -> Domain (mapeia id Integer -> CorretorId)
    @Mapping(source = "id", target = "id", qualifiedByName = "toId")
    Corretor toDomain(CorretorEntity entity);

    // Domain -> Entity (mapeia CorretorId -> Integer)
    @Mapping(source = "id.value", target = "id")
    CorretorEntity toEntity(Corretor domain);

    @Named("toId")
    default CorretorId toId(Integer id) {
        return id == null ? null : new CorretorId(id);
    }
}
