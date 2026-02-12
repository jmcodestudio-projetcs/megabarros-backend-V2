package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CorretorClienteCommandRepository extends Repository<ClienteEntity, Integer> {

    @Query(value = "SELECT cc.id_corretor_cliente FROM corretor_cliente cc WHERE cc.id_corretor = :corretorId AND cc.id_cliente = :clienteId", nativeQuery = true)
    Optional<Integer> findId(@Param("corretorId") Integer corretorId, @Param("clienteId") Integer clienteId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO corretor_cliente (id_corretor, id_cliente) VALUES (:corretorId, :clienteId) ON CONFLICT (id_corretor, id_cliente) DO NOTHING RETURNING id_corretor_cliente", nativeQuery = true)
    Integer insertReturningId(@Param("corretorId") Integer corretorId, @Param("clienteId") Integer clienteId);

    @Query(value = "SELECT COUNT(*) > 0 FROM corretor WHERE id_corretor = :corretorId", nativeQuery = true)
    boolean existsCorretor(@Param("corretorId") Integer corretorId);

    @Query(value = "SELECT COUNT(*) > 0 FROM cliente WHERE id_cliente = :clienteId", nativeQuery = true)
    boolean existsCliente(@Param("clienteId") Integer clienteId);
}
