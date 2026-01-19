package br.com.jmcodestudio.megabarros.application.port.in.corretor;

import br.com.jmcodestudio.megabarros.application.domain.corretor.Corretor;

import java.util.Optional;

/**
 * GetCurrentCorretorUseCase defines the contract for retrieving the currently authenticated Corretor.
 * It encapsulates the business logic required to fetch the current Corretor details from the system.
 *
 * O que faz: define a operação de recuperação do Corretor atualmente autenticado.
 * Onde usar: implementado por serviços de aplicação para lidar com a lógica de recuperação do Corretor atual.
 */
public interface GetCurrentCorretorUseCase {
    Optional<Corretor> findCurrentCorretor();
}
