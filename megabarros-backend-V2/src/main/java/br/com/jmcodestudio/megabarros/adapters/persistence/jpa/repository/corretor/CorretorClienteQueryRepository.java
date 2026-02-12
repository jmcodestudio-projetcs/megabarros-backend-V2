package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.cliente.ClienteEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CorretorClienteQueryRepository extends Repository<ClienteEntity, Integer> {

    @Query(value = "SELECT COUNT(*) > 0 FROM corretor_cliente WHERE id_corretor = :corretorId AND id_cliente = :clienteId", nativeQuery = true)
    boolean existsByCorretorIdAndClienteId(@Param("corretorId") Integer corretorId, @Param("clienteId") Integer clienteId);

    @Query(value = """
        SELECT cc.id_corretor_cliente AS idCorretorCliente,
               cc.id_corretor        AS idCorretor,
               cc.id_cliente         AS idCliente,
               cc.data_inicio        AS dataInicio,
               c.nome_corretor       AS nomeCorretor,
               c.uf                  AS uf,
               u.email               AS email
        FROM corretor_cliente cc
        JOIN corretor c ON c.id_corretor = cc.id_corretor
        JOIN usuario u ON u.id_usuario = c.id_usuario
        WHERE cc.id_cliente = :clienteId
        ORDER BY c.nome_corretor
        """, nativeQuery = true)
    List<CorretorClienteRow> listByClienteId(@Param("clienteId") Integer clienteId);

    @Query(value = """
        SELECT cc.id_corretor_cliente
        FROM corretor_cliente cc
        WHERE cc.id_corretor = :corretorId AND cc.id_cliente = :clienteId
        """, nativeQuery = true)
    Optional<Integer> findIdByCorretorIdAndClienteId(@Param("corretorId") Integer corretorId, @Param("clienteId") Integer clienteId);

    interface CorretorClienteRow {
        Integer getIdCorretorCliente();
        Integer getIdCorretor();
        Integer getIdCliente();
        java.sql.Date getDataInicio();
        String getNomeCorretor();
        String getUf();
        String getEmail();
    }
}