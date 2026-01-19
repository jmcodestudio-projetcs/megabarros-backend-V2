package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.cliente;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Integer> {
    Optional<ClienteEntity> findByCpfCnpj(String cpfCnpj);

    // Lista por corretor via join corretor_cliente
    // Será implementado com query nativa em um repo dedicado para manter simplicidade
}
