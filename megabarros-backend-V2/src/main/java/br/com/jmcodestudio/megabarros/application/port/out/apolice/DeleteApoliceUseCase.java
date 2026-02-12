package br.com.jmcodestudio.megabarros.application.port.out.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;

public interface DeleteApoliceUseCase {
    void deleteById(ApoliceId id);
}
