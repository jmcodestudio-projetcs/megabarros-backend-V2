package br.com.jmcodestudio.megabarros.application.port.out.corretor;

import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;
import br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId;

import java.util.List;
import java.util.Optional;

/**
 * CorretorRepositoryPort defines the contract for persistence operations related to Corretor entities.
 * It abstracts the underlying data storage mechanism, allowing for saving, retrieving, and deleting Corretor records.
 *
 * O que faz: abstrai as operações de persistência para a entidade Corretor.
 * Onde usar: implementado por adaptadores de saída (ex: JPA, JDBC) para interagir com o banco de dados.
 */
public interface CorretorRepositoryPort {
    Corretor save(Corretor corretor);
    Optional<Corretor> findById(CorretorId id);
    List<Corretor> findAll();
    Optional<Corretor> findByUsuarioId(Long usuarioId);
    boolean existsByUsuarioId(Long usuarioId);
    boolean deleteById(CorretorId id);
}
