package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.BeneficiarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiarioJpaRepository extends JpaRepository<BeneficiarioEntity, Integer> {
    List<BeneficiarioEntity> findByIdApolice(Integer idApolice);
}
