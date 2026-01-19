package br.com.jmcodestudio.megabarros.application.port.in.cliente;

import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;

public interface CreateClienteUseCase {
    Cliente create(Cliente cliente);
}
