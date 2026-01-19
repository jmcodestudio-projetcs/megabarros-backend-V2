package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.corretor;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.corretor.CorretorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA Repository para a entidade Corretor.
 */
public interface CorretorJpaRepository extends JpaRepository<CorretorEntity, Integer> {
    Optional<CorretorEntity> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
}
