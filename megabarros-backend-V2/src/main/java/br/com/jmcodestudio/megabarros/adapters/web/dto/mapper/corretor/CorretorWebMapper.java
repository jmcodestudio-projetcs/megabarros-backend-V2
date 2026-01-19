package br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.corretor;

import br.com.jmcodestudio.megabarros.adapters.web.dto.corretor.CorretorCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.corretor.CorretorResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.corretor.CorretorUpdateRequest;
import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CorretorWebMapper {

    // Create: id e dataCriacao não vêm do request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "usuarioId", source = "idUsuario")
    @Mapping(target = "nome", source = "nomeCorretor")
    Corretor toDomain(CorretorCreateRequest req);

    // Update: precisa referenciar o parâmetro 'req' no source
    @Mapping(target = "id", expression = "java(br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId.of(id))")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "usuarioId", source = "req.idUsuario")
    @Mapping(target = "nome", source = "req.nomeCorretor")
    Corretor toDomain(Integer id, CorretorUpdateRequest req);

    // Domain -> Response
    @Mapping(target = "idCorretor", source = "id.value")
    @Mapping(target = "idUsuario", source = "usuarioId")
    @Mapping(target = "nomeCorretor", source = "nome")
    CorretorResponse toResponse(Corretor domain);
}