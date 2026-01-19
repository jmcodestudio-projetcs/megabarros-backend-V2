package br.com.jmcodestudio.megabarros.application.port.in.corretor;

import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;

/**
 * CreateCorretorUseCase defines the contract for creating a new Corretor.
 * It encapsulates the business logic required to add a Corretor to the system.
 *
 * O que faz: define a operação de criação de um novo Corretor.
 * Onde usar: implementado por serviços de aplicação para lidar com a lógica de criação de Corretores.
 */
public interface CreateCorretorUseCase {
    Corretor create(Corretor payload);
}
