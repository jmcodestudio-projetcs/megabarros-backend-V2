package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ApoliceExistenceRepository extends Repository<ApoliceEntity, Integer> {

    @Query(value = "SELECT COUNT(*) > 0 FROM apolice WHERE id_seguradora = :segId", nativeQuery = true)
    boolean existsBySeguradoraId(@Param("segId") Integer segId);

    @Query(value = "SELECT COUNT(*) > 0 FROM apolice WHERE id_produto = :prodId", nativeQuery = true)
    boolean existsByProdutoId(@Param("prodId") Integer prodId);

    @Query(value = "SELECT COUNT(*) FROM apolice WHERE id_seguradora = :segId", nativeQuery = true)
    long countBySeguradoraId(@Param("segId") Integer segId);

    @Query(value = "SELECT COUNT(*) FROM apolice WHERE id_produto = :prodId", nativeQuery = true)
    long countByProdutoId(@Param("prodId") Integer prodId);
}
