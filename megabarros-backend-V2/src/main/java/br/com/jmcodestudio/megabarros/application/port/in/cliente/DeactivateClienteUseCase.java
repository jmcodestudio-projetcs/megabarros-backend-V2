package br.com.jmcodestudio.megabarros.application.port.in.cliente;

import br.com.jmcodestudio.megabarros.application.domain.cliente.ClienteId;

public interface DeactivateClienteUseCase {
    void deactivate(ClienteId id);
}
