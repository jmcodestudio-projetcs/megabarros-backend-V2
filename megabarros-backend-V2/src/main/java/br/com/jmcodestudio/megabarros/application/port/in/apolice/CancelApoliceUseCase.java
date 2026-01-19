package br.com.jmcodestudio.megabarros.application.port.in.apolice;

import br.com.jmcodestudio.megabarros.application.domain.apolice.ApoliceId;

public interface CancelApoliceUseCase {
    void cancel(ApoliceId id, String reason);
}
