package br.com.jmcodestudio.megabarros.adapters.web.dto.mapper.cliente;

import br.com.jmcodestudio.megabarros.adapters.web.dto.cliente.ClienteCreateRequest;
import br.com.jmcodestudio.megabarros.adapters.web.dto.cliente.ClienteResponse;
import br.com.jmcodestudio.megabarros.adapters.web.dto.cliente.ClienteUpdateRequest;
import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;
import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClienteWebMapper {

    default Cliente toDomain(ClienteCreateRequest req) {
        return new Cliente(
                null,
                req.nome(),
                req.cpfCnpj(),
                req.dataNascimento(),
                req.email(),
                req.telefone(),
                true,
                java.util.List.of()
        );
    }

    default Cliente toDomain(Integer id, ClienteUpdateRequest req) {
        return new Cliente(
                new ClienteId(id),
                req.nome(),
                req.cpfCnpj(),
                req.dataNascimento(),
                req.email(),
                req.telefone(),
                null,
                java.util.List.of()
        );
    }

    default ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.id() != null ? c.id().value() : null,
                c.nome(),
                c.cpfCnpj(),
                c.dataNascimento(),
                c.email(),
                c.telefone(),
                c.ativo()
        );
    }
}