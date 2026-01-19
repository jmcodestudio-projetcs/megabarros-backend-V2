package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.cliente;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteQueryRepository extends Repository<ClienteEntity, Integer> {

    @Query(value = """
        SELECT c.id_cliente
        FROM cliente c
        JOIN corretor_cliente cc ON cc.id_cliente = c.id_cliente
        WHERE cc.id_corretor = :corretorId
        """, nativeQuery = true)
    List<Integer> findIdsByCorretorId(@Param("corretorId") Integer corretorId);
}
