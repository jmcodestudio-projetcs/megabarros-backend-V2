package br.com.jmcodestudio.megabarros.application.port.in.corretor;

import br.com.jmcodestudio.megabarros.application.domain.corretor.CorretorId;

/**
 * DeleteCorretorUseCase define o caso de uso para deletar um corretor pelo seu ID.
 */
public interface DeleteCorretorUseCase {
    boolean delete(CorretorId id);
}
