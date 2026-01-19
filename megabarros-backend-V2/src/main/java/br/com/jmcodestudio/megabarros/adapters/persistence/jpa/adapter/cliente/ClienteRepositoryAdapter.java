package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter.cliente;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.mapper.ClientePersistenceMapper;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.cliente.ClienteEnderecoJpaRepository;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.cliente.ClienteJpaRepository;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.cliente.ClienteQueryRepository;
import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;
import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;
import br.com.jmcodestudio.megabarros.application.domain.cliente.Endereco;
import br.com.jmcodestudio.megabarros.application.port.out.cliente.ClienteRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class ClienteRepositoryAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository clienteRepo;
    private final ClienteEnderecoJpaRepository enderecoRepo;
    private final ClienteQueryRepository clienteQueryRepo;
    private final ClientePersistenceMapper mapper;

    public ClienteRepositoryAdapter(ClienteJpaRepository clienteRepo,
                                    ClienteEnderecoJpaRepository enderecoRepo,
                                    ClienteQueryRepository clienteQueryRepo,
                                    ClientePersistenceMapper mapper) {
        this.clienteRepo = clienteRepo;
        this.enderecoRepo = enderecoRepo;
        this.clienteQueryRepo = clienteQueryRepo;
        this.mapper = mapper;
    }

    private Cliente hydrate(ClienteEntity e) {
        List<Endereco> enderecos = enderecoRepo.findByIdCliente(e.getId()).stream().map(mapper::toDomain).toList();
        return mapper.toDomain(e, enderecos);
    }

    @Override
    public Cliente save(Cliente cliente) {
        ClienteEntity e = mapper.toEntity(cliente);
        ClienteEntity s = clienteRepo.save(e);
        return hydrate(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> findById(ClienteId id) {
        return clienteRepo.findById(id.value()).map(this::hydrate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> findByCpfCnpj(String cpfCnpj) {
        return clienteRepo.findByCpfCnpj(cpfCnpj).map(this::hydrate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return clienteRepo.findAll().stream().map(this::hydrate).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> findByCorretorId(Integer corretorId) {
        List<Integer> ids = clienteQueryRepo.findIdsByCorretorId(corretorId);
        return ids.isEmpty() ? List.of()
                : clienteRepo.findAllById(ids).stream().map(this::hydrate).toList();
    }

    @Override
    public void deactivate(ClienteId id) {
        clienteRepo.findById(id.value()).ifPresent(e -> {
            e.setAtivo(Boolean.FALSE);
            clienteRepo.save(e);
        });
    }
}