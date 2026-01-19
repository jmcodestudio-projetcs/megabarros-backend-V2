package br.com.jmcodestudio.megabarros.application.port.in.cliente;

import br.com.jmcodestudio.megabarros.application.domain.cliente.Cliente;

import java.util.List;
import java.util.Optional;

public interface ListClientesUseCase {
    List<Cliente> listAll();
    List<Cliente> listMine(); // para corretor listar apenas seus clientes
    Optional<Cliente> getById(Integer id);
}
