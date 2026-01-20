package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AggregatedApoliceQueryRepository extends Repository<ApoliceEntity, Integer> {

    @Query(value = """
        SELECT a.id_seguradora, COUNT(*) 
        FROM apolice a
        WHERE a.id_seguradora IN (:ids)
        GROUP BY a.id_seguradora
    """, nativeQuery = true)
    List<Object[]> countBySeguradoraIds(@Param("ids") List<Integer> ids);

    @Query(value = """
        SELECT a.id_produto, COUNT(*) 
        FROM apolice a
        WHERE a.id_produto IN (:ids)
        GROUP BY a.id_produto
    """, nativeQuery = true)
    List<Object[]> countByProdutoIds(@Param("ids") List<Integer> ids);
}
