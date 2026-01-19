package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEnderecoEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEntity;
import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;
import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;
import br.com.jmcodestudio.megabarros.application.domain.cliente.Endereco;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientePersistenceMapper {

    default Cliente toDomain(ClienteEntity e, List<Endereco> enderecos) {
        if (e == null) return null;
        return new Cliente(
                ClienteId.of(e.getId()),
                e.getNome(),
                e.getCpfCnpj(),
                e.getDataNascimento(),
                e.getEmail(),
                e.getTelefone(),
                e.getAtivo(),
                enderecos
        );
    }

    default ClienteEntity toEntity(Cliente d) {
        if (d == null) return null;
        ClienteEntity e = new ClienteEntity();
        e.setId(d.id() != null ? d.id().value() : null);
        e.setNome(d.nome());
        e.setCpfCnpj(d.cpfCnpj());
        e.setDataNascimento(d.dataNascimento());
        e.setEmail(d.email());
        e.setTelefone(d.telefone());
        e.setAtivo(d.ativo());
        return e;
    }

    default Endereco toDomain(ClienteEnderecoEntity e) {
        if (e == null) return null;
        return new Endereco(
                e.getId(),
                ClienteId.of(e.getIdCliente()),
                e.getLogradouro(),
                e.getNumero(),
                e.getComplemento(),
                e.getBairro(),
                e.getCidade(),
                e.getUf(),
                e.getCep()
        );
    }
}
