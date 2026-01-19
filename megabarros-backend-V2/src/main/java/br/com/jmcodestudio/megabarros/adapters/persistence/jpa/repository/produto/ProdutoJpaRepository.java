package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.produto;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.produto.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity, Integer> {
    List<ProdutoEntity> findBySeguradora_Id(Integer idSeguradora);
}
