package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.cliente;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEnderecoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteEnderecoJpaRepository extends JpaRepository<ClienteEnderecoEntity, Integer> {
    List<ClienteEnderecoEntity> findByIdCliente(Integer idCliente);
}
