package br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.usuario;

import br.com.jmcodestudio.megabarros.adapters.web.dto.usuario.UsuarioCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.usuario.UsuarioResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.usuario.UsuarioUpdateRequest;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.CreateUsuarioUseCase.UsuarioCreateCommand;
import br.com.jmcodestudio.megabarros.application.port.in.usuario.UpdateUsuarioUseCase.UsuarioUpdateCommand;
import br.com.jmcodestudio.megabarros.application.port.out.UsuarioRepositoryPort;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioWebMapper {

    UsuarioCreateCommand toCommand(UsuarioCreateRequest req);

    UsuarioUpdateCommand toCommand(UsuarioUpdateRequest req);

    @Mapping(target = "idUsuario", source = "id")
    UsuarioResponse toResponse(UsuarioRepositoryPort.UsuarioRecord usuario);
}