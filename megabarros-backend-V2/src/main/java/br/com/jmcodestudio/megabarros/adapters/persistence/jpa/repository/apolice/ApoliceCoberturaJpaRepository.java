package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.apolice;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.apolice.ApoliceCoberturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApoliceCoberturaJpaRepository extends JpaRepository<ApoliceCoberturaEntity, Integer> {
    List<ApoliceCoberturaEntity> findByIdApolice(Integer idApolice);
}
