package br.com.jmcodestudio.megabarros.application.port.in.seguradora;

import br.com.jmcodestudio.megabarros.application.domain.seguradora.Seguradora;
import br.com.jmcodestudio.megabarros.application.domain.seguradora.SeguradoraId;

import java.util.Optional;

public interface UpdateSeguradoraUseCase {
    Optional<Seguradora> update(SeguradoraId id, Seguradora updates);
}
