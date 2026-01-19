package br.com.jmcodestudio.megabarros.application.port.in.seguradora;

import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;

public interface DeleteSeguradoraUseCase {
    void delete(SeguradoraId id);
}
