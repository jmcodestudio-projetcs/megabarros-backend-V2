package br.com.jmcodestudio.megabarros.application.port.in.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.Apolice;
import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;

import java.util.Optional;

public interface UpdateApoliceUseCase {
    Optional<Apolice> update(ApoliceId id, Apolice updates);
}
