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

    // Modifying sem RETURNING: retorna o número de linhas afetadas
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO corretor_cliente (id_corretor, id_cliente) VALUES (:corretorId, :clienteId) ON CONFLICT (id_corretor, id_cliente) DO NOTHING", nativeQuery = true)
    int insertIgnore(@Param("corretorId") Integer corretorId, @Param("clienteId") Integer clienteId);

    @Query(value = "SELECT COUNT(*) > 0 FROM corretor WHERE id_corretor = :corretorId", nativeQuery = true)
    boolean existsCorretor(@Param("corretorId") Integer corretorId);

    @Query(value = "SELECT COUNT(*) > 0 FROM cliente WHERE id_cliente = :clienteId", nativeQuery = true)
    boolean existsCliente(@Param("clienteId") Integer clienteId);

    @Query(value = "SELECT COUNT(*) > 0 FROM corretor_cliente WHERE id_corretor_cliente = :id", nativeQuery = true)
    boolean existsLink(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM corretor_cliente WHERE id_corretor_cliente = :id", nativeQuery = true)
    int deleteById(@Param("id") Integer id);
}