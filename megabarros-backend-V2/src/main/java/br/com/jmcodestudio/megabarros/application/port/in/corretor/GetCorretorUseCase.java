package br.com.jmcodestudio.megabarros.application.port.in.corretor;

import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;
import br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId;

import java.util.List;
import java.util.Optional;

/**
 * GetCorretorUseCase defines the contract for retrieving Corretor entities.
 * It encapsulates the business logic required to fetch Corretor details from the system.
 *
 * O que faz: define as operações de recuperação de Corretores.
 * Onde usar: implementado por serviços de aplicação para lidar com a lógica de recuperação de Corretores.
 */
public interface GetCorretorUseCase {
    List<Corretor> listAll();
    Optional<Corretor> findById(CorretorId id);
}
