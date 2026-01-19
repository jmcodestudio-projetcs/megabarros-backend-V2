package br.com.jmcodestudio.megabarros.application.port.in.cliente;

import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;
import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;

import java.util.Optional;

public interface UpdateClienteUseCase {
    Optional<Cliente> update(ClienteId id, Cliente updates);
}
