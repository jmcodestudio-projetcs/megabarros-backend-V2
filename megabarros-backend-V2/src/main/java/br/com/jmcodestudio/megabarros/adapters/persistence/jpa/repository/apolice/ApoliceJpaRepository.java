package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApoliceJpaRepository extends JpaRepository<ApoliceEntity, Integer> {
    boolean existsByNumero(String numero);
    List<ApoliceEntity> findByIdSeguradora(Integer idSeguradora);
    List<ApoliceEntity> findByIdProduto(Integer idProduto);
    List<ApoliceEntity> findByIdCorretorCliente(Integer idCorretorCliente);
}
