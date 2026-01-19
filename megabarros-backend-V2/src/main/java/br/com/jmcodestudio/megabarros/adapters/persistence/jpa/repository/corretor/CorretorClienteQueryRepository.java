package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface CorretorClienteQueryRepository extends Repository<ClienteEntity, Integer> {

    @Query(value = "SELECT COUNT(*) > 0 FROM corretor_cliente WHERE id_corretor = :corretorId AND id_cliente = :clienteId", nativeQuery = true)
    boolean existsByCorretorIdAndClienteId(@Param("corretorId") Integer corretorId, @Param("clienteId") Integer clienteId);
}