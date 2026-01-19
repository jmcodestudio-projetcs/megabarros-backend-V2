package br.com.jmcodestudio.megabarros.application.port.in.corretor;

import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;
import br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId;

import java.util.Optional;

/**
 * UpdateCorretorUseCase defines the contract for updating an existing Corretor.
 * It encapsulates the business logic required to modify Corretor details in the system.
 *
 * O que faz: define a operação de atualização de um Corretor existente.
 * Onde usar: implementado por serviços de aplicação para lidar com a lógica de atualização de Corretores.
 */
public interface UpdateCorretorUseCase {
    Optional<Corretor> update(CorretorId id, Corretor updates);
}
