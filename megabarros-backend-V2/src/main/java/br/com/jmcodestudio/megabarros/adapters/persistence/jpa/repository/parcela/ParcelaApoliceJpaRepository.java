package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.parcela;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.parcela.ParcelaApoliceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParcelaApoliceJpaRepository extends JpaRepository<ParcelaApoliceEntity, Integer> {
    List<ParcelaApoliceEntity> findByIdApolice(Integer idApolice);
}
