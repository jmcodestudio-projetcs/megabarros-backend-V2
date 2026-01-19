package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.seguradora;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.seguradora.SeguradoraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeguradoraJpaRepository extends JpaRepository<SeguradoraEntity, Integer> {
}
