package br.com.jmcodestudio.megabarros.application.port.out.cliente;

import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;
import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {
    Cliente save(Cliente cliente);
    Optional<Cliente> findById(ClienteId id);
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);
    List<Cliente> findAll();
    List<Cliente> findByCorretorId(Integer corretorId);
    void deactivate(ClienteId id);
}
